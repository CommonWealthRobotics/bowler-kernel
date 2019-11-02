/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
@file:SuppressWarnings("LargeClass")

package com.neuronrobotics.bowlerkernel.hardware.protocol

import arrow.core.Either
import arrow.effects.IO
import arrow.effects.IOOf
import arrow.effects.extensions.io.monad.binding
import arrow.effects.extensions.io.monad.flatten
import arrow.effects.liftIO
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerkernel.deviceserver.DeviceServer
import com.neuronrobotics.bowlerkernel.deviceserver.getPayload
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceType
import com.neuronrobotics.bowlerkernel.internal.logging.LoggerUtilities.Companion.joinWithIndent
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow
import mu.KotlinLogging

/**
 * Parses a received payload into a value. The parameter format is:
 *  `(payload, start index (inclusive), end index (exclusive))`.
 * The valid slice of the payload this method is allowed to access is
 * `payload.sliceArray(start until end)`.
 */
typealias ParseReceivePayload<T> = (ByteArray, Int, Int) -> T

/**
 * An implementation of [BowlerRPCProtocol] using [DeviceServer]. Uses a continuous range of
 * packet ids from [getLowestPacketId] through [getHighestPacketId]. Any numbers outside that
 * range are available for other packets.
 *
 * @param server The [DeviceServer] to communicate with the device with.
 * @param resourceIdValidator The resource id validator used to validate resource ids when adding
 * them during discovery.
 * @param startPacketId The starting id for the packets this class creates.
 */
@SuppressWarnings("TooManyFunctions")
open class DefaultBowlerRPCProtocol(
    private val server: DeviceServer,
    private val resourceIdValidator: ResourceIdValidator,
    private val startPacketId: Byte = DEFAULT_START_PACKET_ID
) : BowlerRPCProtocol {

    private var highestPacketId = AtomicInteger(startPacketId.toInt())
    private var highestGroupId = AtomicInteger(1)

    private val groupedResourceToGroupId = mutableMapOf<ResourceId, Int>()
    private val groupIdToMembers = mutableMapOf<Int, MutableSet<ResourceId>>()

    private val nonGroupedResourceIdToPacketId = mutableMapOf<ResourceId, Int>()

    private val groupIdToPacketId = mutableMapOf<Int, Int>()
    private val groupIdToCount = mutableMapOf<Int, Int>()
    private val groupMemberToSendRange = mutableMapOf<ResourceId, Pair<Byte, Byte>>()
    private val groupMemberToReceiveRange = mutableMapOf<ResourceId, Pair<Byte, Byte>>()

    /**
     * Whether the device is connected.
     */
    private var isConnected = false

    init {
        require(startPacketId > 0) {
            "The starting packet id ($startPacketId) must be greater than zero."
        }

        require(startPacketId != DISCOVERY_PACKET_ID) {
            "The starting packet id ($startPacketId) cannot be equal to the discovery packet id " +
                "($DISCOVERY_PACKET_ID)."
        }
    }

    /**
     * Sends a general discovery packet.
     *
     * Section 2.1.1.
     *
     * @param operation The operation.
     * @param payload The payload.
     * @return The reply payload.
     */
    private fun sendGeneralDiscoveryPacket(
        operation: Byte,
        payload: ByteArray
    ): IO<ByteArray> = IO {
        val payloadWithHeader = getPayload(PAYLOAD_SIZE, byteArrayOf(operation) + payload)

        LOGGER.debug {
            """
            |Sent discovery packet:
            |${payloadWithHeader.joinToString()}
            """.trimMargin()
        }

        server.write(DISCOVERY_PACKET_ID, payloadWithHeader).map {
            LOGGER.debug {
                """
                |Discovery response:
                |${it.joinToString()}
                """.trimMargin()
            }

            it
        }
    }.flatten()

    /**
     * Sends a discovery packet.
     *
     * Section 2.1.2.
     *
     * @param packetId The new ID for the packet being discovered.
     * @param resource The type of the resource.
     * @param attachment The type of the attachment point.
     * @param attachmentData Any data needed to fully describe the attachment.
     * @return The reply payload.
     */
    private fun sendDiscoveryPacket(
        packetId: Byte,
        resource: Byte,
        attachment: Byte,
        attachmentData: ByteArray
    ) = sendGeneralDiscoveryPacket(
        OPERATION_DISCOVERY_ID,
        byteArrayOf(packetId, resource, attachment) + attachmentData
    )

    /**
     * Sends a group discovery packet.
     *
     * Section 2.1.3.
     *
     * @param groupId The ID for the group being made.
     * @param packetId The ID for the packet the group will use.
     * @param count The number of resources that will be added to the group.
     * @return The reply payload.
     */
    private fun sendGroupDiscoveryPacket(
        groupId: Byte,
        packetId: Byte,
        count: Byte
    ) = sendGeneralDiscoveryPacket(
        OPERATION_GROUP_DISCOVERY_ID,
        byteArrayOf(groupId, packetId, count)
    )

    /**
     * Sends a group member discovery packet.
     *
     * Section 2.1.4.
     *
     * @param groupId The ID for the group that this resource will be added to.
     * @param sendStart The starting byte index in the send payload for this resource's write data.
     * @param sendEnd The ending byte index in the send payload for this resource's write data.
     * @param receiveStart The starting byte index in the receive payload for this resource's
     * read data.
     * @param receiveEnd The ending byte index in the receive payload for this resource's read data.
     * @param resource The type of the resource.
     * @param attachment The type of the attachment point.
     * @param attachmentData Any data needed to fully describe the attachment.
     * @return The reply payload.
     */
    @SuppressWarnings("LongParameterList")
    private fun sendGroupMemberDiscoveryPacket(
        groupId: Byte,
        sendStart: Byte,
        sendEnd: Byte,
        receiveStart: Byte,
        receiveEnd: Byte,
        resource: Byte,
        attachment: Byte,
        attachmentData: ByteArray
    ) = sendGeneralDiscoveryPacket(
        OPERATION_GROUP_MEMBER_DISCOVERY_ID,
        byteArrayOf(
            groupId, sendStart, sendEnd, receiveStart, receiveEnd, resource, attachment
        ) + attachmentData
    )

    /**
     * Sends a discard discovery packet.
     *
     * Section 2.1.5.
     *
     * @return The reply payload.
     */
    private fun sendDiscardDiscoveryPacket() =
        sendGeneralDiscoveryPacket(OPERATION_DISCARD_DISCOVERY_ID, byteArrayOf())

    /**
     * Adds a group by sending a group discovery packet followed by a group member discovery
     * packet for each element of [resourceIds]. Fails-fast when discovering group members.
     *
     * @param resourceIds The group members.
     */
    private fun addGroup(
        resourceIds: ImmutableSet<ResourceId>
    ): IO<Unit> = IO {
        LOGGER.debug {
            """
            |Adding group:
            |resourceIds:
            |${resourceIds.joinWithIndent("\t")}
            """.trimMargin()
        }

        val groupId = getNextGroupId()
        val packetId = getNextPacketId()
        val count = resourceIds.size

        val groupStatus = sendGroupDiscoveryPacket(
            groupId.toByte(),
            packetId.toByte(),
            count.toByte()
        )

        groupStatus.ifAccepted {
            groupIdToPacketId[groupId] = packetId
            groupIdToCount[groupId] = count

            var currentSendIndex = 0.toByte()
            var currentReceiveIndex = 0.toByte()

            val allResources = binding {
                resourceIds.forEach { resourceId ->
                    val sendLength = resourceId.resourceType.sendLength
                    val receiveLength = resourceId.resourceType.receiveLength

                    if (isGreaterThanUnsignedByte(currentSendIndex + sendLength) ||
                        isGreaterThanUnsignedByte(currentReceiveIndex + receiveLength)
                    ) {
                        IO.raiseError<Unit>(
                            IllegalStateException(
                                """
                                |Cannot handle payload indices greater than a byte.
                                |Send index: $currentSendIndex
                                |Receive index: $currentReceiveIndex
                                """.trimMargin()
                            )
                        ).bind()
                    }

                    if (currentSendIndex + sendLength > PAYLOAD_SIZE ||
                        currentReceiveIndex + receiveLength > PAYLOAD_SIZE
                    ) {
                        IO.raiseError<Unit>(
                            IllegalStateException(
                                """
                                |Cannot handle payload indices greater than the payload size.
                                |Payload size: $PAYLOAD_SIZE
                                |Send index: $currentSendIndex
                                |Receive index: $currentReceiveIndex
                                """.trimMargin()
                            )
                        ).bind()
                    }

                    val sendStart = currentSendIndex
                    val sendEnd = (currentSendIndex + sendLength).toByte()
                    val receiveStart = currentReceiveIndex
                    val receiveEnd = (currentReceiveIndex + receiveLength).toByte()

                    sendGroupMemberDiscoveryPacket(
                        groupId.toByte(),
                        sendStart,
                        sendEnd,
                        receiveStart,
                        receiveEnd,
                        resourceId.resourceType.type,
                        resourceId.attachmentPoint.type,
                        resourceId.attachmentPoint.data
                    ).ifAccepted {
                        groupedResourceToGroupId[resourceId] = groupId
                        groupIdToMembers.getOrPut(groupId) {
                            mutableSetOf()
                        }.add(resourceId)
                        groupMemberToSendRange[resourceId] = sendStart to sendEnd
                        groupMemberToReceiveRange[resourceId] = receiveStart to receiveEnd

                        currentSendIndex = (currentSendIndex + sendLength).toByte()
                        currentReceiveIndex = (currentReceiveIndex + receiveLength).toByte()

                        IO.just(Unit)
                    }.bind()
                }
            }

            allResources.flatMap {
                IO {
                    // TODO: Expose maxRetries
                    server.addUnreliable(packetId.toByte(), 10)
                }
            }
        }
    }.flatten()

    /**
     * Adds a non-polling resource by sending a discovery packet.
     *
     * @param resourceId The resource id.
     */
    private fun addResource(
        resourceId: ResourceId
    ): IO<Unit> = IO {
        LOGGER.debug {
            """
            |Adding resource:
            |resourceId: $resourceId
            """.trimMargin()
        }

        val packetId = getNextPacketId()

        val status = sendDiscoveryPacket(
            packetId.toByte(),
            resourceId.resourceType.type,
            resourceId.attachmentPoint.type,
            resourceId.attachmentPoint.data
        )

        status.ifAccepted {
            IO {
                // Discovery packet was accepted
                nonGroupedResourceIdToPacketId[resourceId] = packetId

                // TODO: Expose maxRetries
                server.addUnreliable(packetId.toByte(), 10)
            }
        }
    }.flatten()

    /**
     * Computes the next packet id and validates that it can be sent to the device.
     *
     * @return The next packet id.
     */
    private fun getNextPacketId(): Int {
        val id = highestPacketId.getAndIncrement()

        check(!isGreaterThanUnsignedByte(id)) { "Cannot handle packet ids greater than a byte." }

        return id
    }

    /**
     * Computes the next group id and validates that it can be sent to the device.
     *
     * @return The next group id.
     */
    private fun getNextGroupId(): Int {
        val id = highestGroupId.getAndIncrement()

        check(!isGreaterThanUnsignedByte(id)) { "Cannot handle group ids greater than a byte." }

        return id
    }

    /**
     * Validates that all resources are part of the same group, there are the correct number of
     * resources, and all resources in the group are present. Returns the group id if validation
     * succeeded.
     *
     * @param resourcesAndValues The group members and their associated values to send.
     * @return The group id.
     */
    private fun <T> validateGroupSendResources(
        resourcesAndValues: List<Pair<ResourceId, T>>
    ): IO<Int> = IO {
        getValidatedGroupId(resourcesAndValues.first().first).flatMap { groupId ->
            if (resourcesAndValues.size != groupIdToCount[groupId]) {
                return@flatMap IO.raiseError<Int>(
                    UnsupportedOperationException("Mismatched group size.")
                )
            }

            if (!resourcesAndValues.all { groupedResourceToGroupId[it.first] == groupId }) {
                return@flatMap IO.raiseError<Int>(
                    UnsupportedOperationException("Mismatched group ids.")
                )
            }

            if (resourcesAndValues.mapTo(LinkedHashSet(resourcesAndValues.size)) { it.first } !=
                groupIdToMembers[groupId]) {
                return@flatMap IO.raiseError<Int>(
                    UnsupportedOperationException("Mismatched group members.")
                )
            }

            IO.just(groupId)
        }
    }.flatten()

    /**
     * Validates that all resources are part of the same group, there are the correct number of
     * resources, and all resources in the group are present. Returns the group id if validation
     * succeeded.
     *
     * @param resourceIds The group members.
     * @return The group id.
     */
    private fun validateGroupReceiveResources(resourceIds: List<ResourceId>): IO<Int> = IO {
        getValidatedGroupId(resourceIds.first()).flatMap { groupId ->
            if (resourceIds.size != groupIdToCount[groupId]) {
                return@flatMap IO.raiseError<Int>(
                    UnsupportedOperationException("Mismatched group size.")
                )
            }

            if (!resourceIds.all { groupedResourceToGroupId[it] == groupId }) {
                return@flatMap IO.raiseError<Int>(
                    UnsupportedOperationException("Mismatched group ids.")
                )
            }

            if (resourceIds.toSet() != groupIdToMembers[groupId]) {
                return@flatMap IO.raiseError<Int>(
                    UnsupportedOperationException("Mismatched group members.")
                )
            }

            IO.just(groupId)
        }
    }.flatten()

    /**
     * Maps the [resourceId] to its group id with validation.
     *
     * @param resourceId The resource id.
     * @return The group id.
     */
    private fun getValidatedGroupId(resourceId: ResourceId): IO<Int> {
        return groupedResourceToGroupId[resourceId]?.let { IO.just(it) } ?: IO.raiseError(
            UnsupportedOperationException(
                """
                |The resource id was not discovered:
                |$resourceId
                """.trimMargin()
            )
        )
    }

    /**
     * Creates the send payload for a group. Sorts the [resourcesAndValues] into the correct
     * ordering based on their send indices.
     *
     * @param resourcesAndValues The group members and their associated values to send.
     * @param makeResourcePayload Creates the payload for one resource given its value.
     * @return The payload.
     */
    private fun <T> makeGroupSendPayload(
        resourcesAndValues: List<Pair<ResourceId, T>>,
        makeResourcePayload: (T) -> ByteArray
    ): ByteArray {
        return resourcesAndValues.sortedWith(Comparator { first, second ->
            groupMemberToSendRange[first.first]!!.first -
                groupMemberToSendRange[second.first]!!.first
        }).fold(byteArrayOf()) { acc, (a, value) ->
            val sendRange = groupMemberToSendRange[a]!!
            acc + makeResourcePayload(value).sliceArray(0 until sendRange.second - sendRange.first)
        }
    }

    /**
     * Parses an entire receive payload for a group.
     *
     * @param resourceIds The group members.
     * @param fullPayload The entire receive payload.
     * @param parseResourcePayload Parses one member's section of the payload. Gives the full
     * payload and the starting and ending indices of that a member's data.
     * @return The parsed values in the same order as [resourceIds].
     */
    private fun <T> parseGroupReceivePayload(
        resourceIds: List<ResourceId>,
        fullPayload: ByteArray,
        parseResourcePayload: ParseReceivePayload<T>
    ): List<T> {
        return resourceIds.map {
            val receiveRange = groupMemberToReceiveRange[it]!!
            parseResourcePayload(
                fullPayload,
                receiveRange.first.toInt(),
                receiveRange.second.toInt()
            )
        }
    }

    /**
     * Performs a full RPC read call:
     * 1. Get the packet id
     * 2. Write to the [server]
     * 4. Call [parseReceivePayload] and return the result
     *
     * @param resourceId The resource id.
     * @param parseReceivePayload Parses the receive payload into a value.
     * @return The value returned from [parseReceivePayload].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun <T> handleRead(
        resourceId: ResourceId,
        parseReceivePayload: ParseReceivePayload<T>
    ): IO<T> = IO {
        getValidatedPacketId(resourceId)
    }.flatMap { packetId ->
        server.write(packetId.toByte(), ByteArray(PAYLOAD_SIZE) { 0 }).map {
            parseReceivePayload(it, 0, it.size)
        }
    }

    /**
     * Performs a full RPC group read call:
     * 1. Validate the group and get the group id
     * 2. Get the packet id
     * 3. Write to the [server]
     * 5. Call [parseGroupReceivePayload] with [parseReceivePayload] and return the result
     *
     * @param resourceIds The group members.
     * @param parseReceivePayload Parses a member's section of the group receive payload.
     * @return The values from [parseReceivePayload] in the same order as [resourceIds].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun <T> handleGroupRead(
        resourceIds: List<ResourceId>,
        parseReceivePayload: ParseReceivePayload<T>
    ): IO<List<T>> = validateGroupReceiveResources(resourceIds).flatMap { groupId ->
        val packetId = groupIdToPacketId[groupId]!!
        server.write(packetId.toByte(), ByteArray(PAYLOAD_SIZE) { 0 }).map {
            parseGroupReceivePayload(
                resourceIds,
                it,
                parseReceivePayload
            )
        }
    }

    /**
     * Performs a full RPC write call:
     * 1. Get the packet id
     * 2. Write to the [server]
     *
     * @param resourceId The resource id.
     * @param value The value to write.
     * @param makeSendPayload Makes a send payload from a value.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun <S> handleWrite(
        resourceId: ResourceId,
        value: S,
        makeSendPayload: (S) -> ByteArray
    ): IO<Unit> = IO {
        getValidatedPacketId(resourceId)
    }.flatMap { packetId ->
        server.write(packetId.toByte(), getPayload(PAYLOAD_SIZE, makeSendPayload(value)))
            .map { Unit }
    }

    /**
     * Performs a full RPC write call:
     * 1. Get the packet id
     * 2. Write to the [server]
     * 4. Call [parseReceivePayload] and return the result
     *
     * @param resourceId The resource id.
     * @param value The value to write.
     * @param makeSendPayload Makes a send payload from a value.
     * @param parseReceivePayload Parses the receive payload into a value.
     * @return The value from [parseReceivePayload].
     */
    protected fun <S, R> handleWrite(
        resourceId: ResourceId,
        value: S,
        makeSendPayload: (S) -> ByteArray,
        parseReceivePayload: ParseReceivePayload<R>
    ): IO<R> = IO {
        getValidatedPacketId(resourceId)
    }.flatMap { packetId ->
        server.write(packetId.toByte(), getPayload(PAYLOAD_SIZE, makeSendPayload(value)))
            .map {
                parseReceivePayload(it, 0, it.size)
            }
    }

    /**
     * Maps the [resourceId] to its packet id with validation.
     *
     * @param resourceId The resource id.
     * @return The packet id.
     */
    private fun getValidatedPacketId(resourceId: ResourceId) =
        nonGroupedResourceIdToPacketId[resourceId] ?: throw IllegalArgumentException(
            """
            |The resource id was not discovered:
            |$resourceId
            """.trimMargin()
        )

    /**
     * Performs a full RPC group write call:
     * 1. Validate the group and get the group id
     * 2. Get the packet id
     * 3. Call [makeGroupSendPayload] with [makeSendPayload]
     * 4. Write to the [server]
     *
     * @param resourcesAndValues The resource ids paired with their values.
     * @param makeSendPayload Makes a send payload from a value.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun <T> handleGroupWrite(
        resourcesAndValues: List<Pair<ResourceId, T>>,
        makeSendPayload: (T) -> ByteArray
    ): IO<Unit> = validateGroupSendResources(resourcesAndValues).flatMap { groupId ->
        val packetId = groupIdToPacketId[groupId]!!
        server.write(
            packetId.toByte(),
            getPayload(PAYLOAD_SIZE, makeGroupSendPayload(resourcesAndValues, makeSendPayload))
        ).map { Unit }
    }

    /**
     * Performs a full RPC group write call:
     * 1. Validate the group and get the group id
     * 2. Get the packet id
     * 3. Call [makeGroupSendPayload] with [makeSendPayload]
     * 4. Write to the [server]
     * 5. Call [parseGroupReceivePayload] with [parseReceivePayload] and return the result
     *
     * @param resourcesAndValues The resource ids paired with their values.
     * @param makeSendPayload Makes a send payload from a value.
     * @param parseReceivePayload Parses the receive payload into a value.
     * @return The values from [parseReceivePayload] in the same order as [resourcesAndValues].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun <T, R> handleGroupWrite(
        resourcesAndValues: List<Pair<ResourceId, T>>,
        makeSendPayload: (T) -> ByteArray,
        parseReceivePayload: ParseReceivePayload<R>
    ): IO<List<R>> = validateGroupSendResources(resourcesAndValues).flatMap { groupId ->
        val packetId = groupIdToPacketId[groupId]!!
        server.write(
            packetId.toByte(),
            getPayload(PAYLOAD_SIZE, makeGroupSendPayload(resourcesAndValues, makeSendPayload))
        ).map {
            parseGroupReceivePayload(
                resourcesAndValues.map { it.first },
                it,
                parseReceivePayload
            )
        }
    }

    /**
     * Flatmaps over [this] with [f] if the payload was accepted. Errors otherwise.
     */
    protected fun <A> IO<ByteArray>.ifAccepted(f: (ByteArray) -> IOOf<A>): IO<A> =
        flatMap {
            if (it[0] == STATUS_ACCEPTED) {
                f(it)
            } else {
                IO.raiseError(
                    UnsupportedOperationException(
                        "${strerror(it[0])}\n${it.joinToString()}"
                    )
                )
            }
        }

    override fun connect() = server.connect().map {
        server.addReliable(DISCOVERY_PACKET_ID)
        isConnected = true
    }

    override fun disconnect(): IO<Unit> {
        if (!isConnected) {
            return server.disconnect().map { isConnected = false }
        }

        return IO.tailRecM(sendDiscardDiscoveryPacket()) { a ->
            a.map {
                if (it[0] == STATUS_DISCARD_COMPLETE) {
                    Either.Right(Unit)
                } else {
                    Either.Left(
                        IO.defer {
                            Thread.sleep(100)
                            sendDiscardDiscoveryPacket()
                        }
                    )
                }
            }
        }.flatMap {
            server.disconnect().map { isConnected = false }
        }
    }

    override fun addRead(resourceId: ResourceId) =
        resourceIdValidator.validateIsReadType(resourceId.resourceType).liftIO().flatMap {
            it.fold(
                { IO.raiseError<Unit>(UnsupportedOperationException(it)) },
                { addResource(resourceId) }
            )
        }

    override fun addReadGroup(resourceIds: ImmutableSet<ResourceId>) =
        validateResources(resourceIds, ResourceIdValidator::validateIsReadType).liftIO().flatMap {
            if (it.isNotEmpty()) {
                IO.raiseError(
                    UnsupportedOperationException(
                        """
                        |Found invalid resources when trying to add a read group:
                        |${it.joinToString("\n")}
                        """.trimMargin()
                    )
                )
            } else {
                addGroup(resourceIds)
            }
        }

    override fun addWrite(resourceId: ResourceId) =
        resourceIdValidator.validateIsWriteType(resourceId.resourceType).liftIO().flatMap {
            it.fold(
                { IO.raiseError<Unit>(UnsupportedOperationException(it)) },
                { addResource(resourceId) }
            )
        }

    override fun addWriteGroup(resourceIds: ImmutableSet<ResourceId>) =
        validateResources(resourceIds, ResourceIdValidator::validateIsWriteType).liftIO().flatMap {
            if (it.isNotEmpty()) {
                IO.raiseError(
                    UnsupportedOperationException(
                        """
                        |Found invalid resources when trying to add a write group:
                        |${it.joinToString("\n")}
                        """.trimMargin()
                    )
                )
            } else {
                addGroup(resourceIds)
            }
        }

    override fun addWriteRead(resourceId: ResourceId): IO<Unit> =
        resourceIdValidator.validateIsWriteType(resourceId.resourceType).liftIO().flatMap {
            it.fold(
                { IO.raiseError<Unit>(UnsupportedOperationException(it)) },
                {
                    resourceIdValidator.validateIsReadType(resourceId.resourceType).liftIO()
                        .flatMap {
                            it.fold(
                                { IO.raiseError<Unit>(UnsupportedOperationException(it)) },
                                { addResource(resourceId) }
                            )
                        }
                }
            )
        }

    override fun addWriteReadGroup(resourceIds: ImmutableSet<ResourceId>) =
        validateResources(resourceIds, ResourceIdValidator::validateIsWriteType).liftIO().flatMap {
            if (it.isNotEmpty()) {
                IO.raiseError(
                    UnsupportedOperationException(
                        """
                        |Found invalid resources when trying to add a write+read group:
                        |${it.joinToString("\n")}
                        """.trimMargin()
                    )
                )
            } else {
                validateResources(resourceIds, ResourceIdValidator::validateIsReadType).liftIO()
                    .flatMap {
                        if (it.isNotEmpty()) {
                            IO.raiseError(
                                UnsupportedOperationException(
                                    """
                                    |Found invalid resources when trying to add a write+read group:
                                    |${it.joinToString("\n")}
                                    """.trimMargin()
                                )
                            )
                        } else {
                            addGroup(resourceIds)
                        }
                    }
            }
        }

    /**
     * Validates that all the resources are of a specific type.
     *
     * @param resourceIds The resources to validate.
     * @param validator The validation method.
     * @return The result of validation for invalid resources.
     */
    private fun validateResources(
        resourceIds: ImmutableSet<ResourceId>,
        validator: ResourceIdValidator.(ResourceType) -> Either<String, Unit>
    ): List<Either<String, Unit>> {
        return resourceIds.map {
            resourceIdValidator.validator(it.resourceType)
        }.filter {
            it.isLeft()
        }
    }

    override fun isResourceInRange(resourceId: ResourceId): IO<Boolean> = IO.just(true)

    override fun readProtocolVersion(): IO<String> {
        TODO("not implemented")
    }

    @Suppress("UNUSED_PARAMETER")
    protected fun parseAnalogReadPayload(payload: ByteArray, start: Int, end: Int): Double {
        val buffer = ByteBuffer.allocate(Short.SIZE_BYTES)
        buffer.put(payload[start])
        buffer.put(payload[start + 1])
        buffer.rewind()
        return buffer.char.toDouble()
    }

    override fun analogRead(resourceId: ResourceId) =
        handleRead(resourceId, this::parseAnalogReadPayload)

    override fun analogRead(resourceIds: List<ResourceId>) =
        handleGroupRead(resourceIds, this::parseAnalogReadPayload)

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun makeAnalogWritePayload(value: Short): ByteArray {
        val buffer = ByteBuffer.allocate(Short.SIZE_BYTES)
        buffer.putShort(value)
        return buffer.array()
    }

    override fun analogWrite(resourceId: ResourceId, value: Short) =
        handleWrite(resourceId, value, this::makeAnalogWritePayload)

    override fun analogWrite(resourcesAndValues: List<Pair<ResourceId, Short>>) =
        handleGroupWrite(resourcesAndValues, this::makeAnalogWritePayload)

    @Suppress("UNUSED_PARAMETER")
    protected fun parseButtonReadPayload(payload: ByteArray, start: Int, end: Int): Boolean =
        payload[start] == 0.toByte()

    override fun buttonRead(resourceId: ResourceId) =
        handleRead(resourceId, this::parseButtonReadPayload)

    override fun buttonRead(resourceIds: List<ResourceId>) =
        handleGroupRead(resourceIds, this::parseButtonReadPayload)

    @Suppress("UNUSED_PARAMETER")
    protected fun parseDigitalReadPayload(payload: ByteArray, start: Int, end: Int): DigitalState {
        return if (payload[start] == 0.toByte()) {
            DigitalState.LOW
        } else {
            DigitalState.HIGH
        }
    }

    override fun digitalRead(resourceId: ResourceId) =
        handleRead(resourceId, this::parseDigitalReadPayload)

    override fun digitalRead(resourceIds: List<ResourceId>) =
        handleGroupRead(resourceIds, this::parseDigitalReadPayload)

    protected fun makeDigitalWritePayload(value: DigitalState): ByteArray {
        val buffer = ByteBuffer.allocate(1)
        buffer.put(value.byte)
        return buffer.array()
    }

    override fun digitalWrite(resourceId: ResourceId, value: DigitalState) =
        handleWrite(resourceId, value, this::makeDigitalWritePayload)

    override fun digitalWrite(resourcesAndValues: List<Pair<ResourceId, DigitalState>>) =
        handleGroupWrite(resourcesAndValues, this::makeDigitalWritePayload)

    @Suppress("UNUSED_PARAMETER")
    protected fun parseEncoderReadPayload(payload: ByteArray, start: Int, end: Int): Long {
        TODO()
    }

    override fun encoderRead(resourceId: ResourceId) =
        handleRead(resourceId, this::parseEncoderReadPayload)

    override fun encoderRead(resourceIds: List<ResourceId>) =
        handleGroupRead(resourceIds, this::parseEncoderReadPayload)

    @Suppress("UNUSED_PARAMETER")
    protected fun makeToneWritePayload(frequencyAndDuration: Pair<Int, Long>): ByteArray {
        TODO()
    }

    override fun toneWrite(resourceId: ResourceId, frequency: Int) =
        handleWrite(resourceId, frequency to (-1).toLong(), this::makeToneWritePayload)

    override fun toneWrite(resourceId: ResourceId, frequency: Int, duration: Long) =
        handleWrite(resourceId, frequency to duration, this::makeToneWritePayload)

    @Suppress("UNUSED_PARAMETER")
    protected fun makeSerialWritePayload(message: String): ByteArray {
        TODO()
    }

    override fun serialWrite(resourceId: ResourceId, message: String) =
        handleWrite(resourceId, message, this::makeSerialWritePayload)

    @Suppress("UNUSED_PARAMETER")
    protected fun parseSerialReadPayload(payload: ByteArray, start: Int, end: Int): String {
        TODO()
    }

    override fun serialRead(resourceId: ResourceId) =
        handleRead(resourceId, this::parseSerialReadPayload)

    @Suppress("UNUSED_PARAMETER")
    protected fun makeServoWritePayload(angle: Double): ByteArray {
        val buffer = ByteBuffer.allocate(Short.SIZE_BYTES)
        buffer.putShort(angle.toShort())
        return buffer.array()
    }

    override fun servoWrite(resourceId: ResourceId, angle: Double) =
        handleWrite(resourceId, angle, this::makeServoWritePayload)

    override fun servoWrite(resourcesAndValues: List<Pair<ResourceId, Double>>) =
        handleGroupWrite(resourcesAndValues, this::makeServoWritePayload)

    @Suppress("UNUSED_PARAMETER")
    protected fun makeStepperWritePayload(stepsAndSpeed: Pair<Int, Int>): ByteArray {
        TODO()
    }

    override fun stepperWrite(resourceId: ResourceId, steps: Int, speed: Int) =
        handleWrite(resourceId, steps to speed, this::makeStepperWritePayload)

    override fun stepperWrite(resourcesAndValues: List<Pair<ResourceId, Pair<Int, Int>>>) =
        handleGroupWrite(resourcesAndValues, this::makeStepperWritePayload)

    @Suppress("UNUSED_PARAMETER")
    protected fun parseUltrasonicReadPayload(payload: ByteArray, start: Int, end: Int): Long {
        TODO()
    }

    override fun ultrasonicRead(resourceId: ResourceId) =
        handleRead(resourceId, this::parseUltrasonicReadPayload)

    override fun ultrasonicRead(resourceIds: List<ResourceId>) =
        handleGroupRead(resourceIds, this::parseUltrasonicReadPayload)

    protected fun parseGenericReadPayload(payload: ByteArray, start: Int, end: Int) =
        payload.sliceArray(start until end)

    protected fun makeGenericWritePayload(payload: ByteArray) =
        payload

    override fun genericRead(resourceId: ResourceId) =
        handleRead(resourceId, this::parseGenericReadPayload)

    override fun genericRead(resourceIds: List<ResourceId>) =
        handleGroupRead(resourceIds, this::parseGenericReadPayload)

    override fun genericWrite(resourceId: ResourceId, payload: ByteArray) =
        handleWrite(resourceId, payload, this::makeGenericWritePayload)

    override fun genericWrite(resourcesAndValues: List<Pair<ResourceId, ByteArray>>) =
        handleGroupWrite(resourcesAndValues, this::makeGenericWritePayload)

    override fun genericWriteRead(resourceId: ResourceId, payload: ByteArray) =
        handleWrite(
            resourceId,
            payload,
            this::makeGenericWritePayload,
            this::parseGenericReadPayload
        )

    override fun genericWriteRead(resourcesAndValues: List<Pair<ResourceId, ByteArray>>) =
        handleGroupWrite(
            resourcesAndValues,
            this::makeGenericWritePayload,
            this::parseGenericReadPayload
        )

    override fun strerror(error: Byte) = when (error) {
        STATUS_ACCEPTED -> "Payload accepted."
        STATUS_REJECTED_GENERIC -> "Generic payload rejection."
        STATUS_REJECTED_UNKNOWN_RESOURCE -> "Unknown resource."
        STATUS_REJECTED_UNKNOWN_ATTACHMENT -> "Unknown attachment."
        STATUS_REJECTED_INVALID_ATTACHMENT -> "Invalid attachment."
        STATUS_REJECTED_INVALID_ATTACHMENT_DATA -> "Invalid attachment data."
        STATUS_REJECTED_INVALID_GROUP_ID -> "Invalid group id."
        STATUS_REJECTED_GROUP_FULL -> "Group full."
        STATUS_REJECTED_UNKNOWN_OPERATION -> "Unknown operation."
        STATUS_DISCARD_IN_PROGRESS -> "Discard in progress."
        STATUS_DISCARD_COMPLETE -> "Discard complete."
        STATUS_REJECTED_INVALID_PACKET_ID -> "Invalid packet id."
        else -> "Unknown error."
    }

    /**
     * The lowest packet id.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getLowestPacketId(): Byte = startPacketId

    /**
     * The highest packet id.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getHighestPacketId(): Byte = highestPacketId.get().toByte()

    companion object {

        /**
         * The id of the discovery packet.
         */
        const val DISCOVERY_PACKET_ID: Byte = 2

        /**
         * The default starting packet id.
         */
        const val DEFAULT_START_PACKET_ID: Byte = (DISCOVERY_PACKET_ID + 1).toByte()

        /**
         * The size of a payload in bytes.
         */
        const val PAYLOAD_SIZE = 61

        /**
         * The size of a packet in bytes.
         */
        const val PACKET_SIZE = PAYLOAD_SIZE + 3

        /**
         * The operation ID's.
         */
        const val OPERATION_DISCOVERY_ID: Byte = 1
        const val OPERATION_GROUP_DISCOVERY_ID: Byte = 2
        const val OPERATION_GROUP_MEMBER_DISCOVERY_ID: Byte = 3
        const val OPERATION_DISCARD_DISCOVERY_ID: Byte = 4

        /**
         * The status codes.
         */
        const val STATUS_ACCEPTED: Byte = 1
        const val STATUS_REJECTED_GENERIC: Byte = 2
        const val STATUS_REJECTED_UNKNOWN_RESOURCE: Byte = 3
        const val STATUS_REJECTED_UNKNOWN_ATTACHMENT: Byte = 4
        const val STATUS_REJECTED_INVALID_ATTACHMENT: Byte = 5
        const val STATUS_REJECTED_INVALID_ATTACHMENT_DATA: Byte = 6
        const val STATUS_REJECTED_INVALID_GROUP_ID: Byte = 7
        const val STATUS_REJECTED_GROUP_FULL: Byte = 8
        const val STATUS_REJECTED_UNKNOWN_OPERATION: Byte = 9
        const val STATUS_DISCARD_IN_PROGRESS: Byte = 10
        const val STATUS_DISCARD_COMPLETE: Byte = 11
        const val STATUS_REJECTED_INVALID_PACKET_ID: Byte = 12

        private val LOGGER = KotlinLogging.logger { }

        /**
         * Computes whether the [testNumber] is outside the range of a unsigned byte.
         *
         * @param testNumber The test number.
         * @return Whether the [testNumber] is outside the range of an unsigned byte.
         */
        internal fun isGreaterThanUnsignedByte(testNumber: Int) =
            testNumber > 2.0.pow(Byte.SIZE_BITS) - 1
    }
}
