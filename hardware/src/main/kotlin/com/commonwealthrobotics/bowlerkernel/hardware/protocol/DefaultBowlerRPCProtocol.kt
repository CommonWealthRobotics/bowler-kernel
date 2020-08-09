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
package com.commonwealthrobotics.bowlerkernel.hardware.protocol

import arrow.core.Either
import arrow.fx.IO
import arrow.fx.extensions.fx
import com.commonwealthrobotics.bowlerkernel.deviceserver.DeviceServer
import com.commonwealthrobotics.bowlerkernel.deviceserver.getPayload
import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceType
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

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
 * @param startPacketId The starting id for the packets this class creates.
 */
@SuppressWarnings("TooManyFunctions")
open class DefaultBowlerRPCProtocol(
    private val server: DeviceServer,
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
    ): IO<ByteArray> {
        val payloadWithHeader = getPayload(PAYLOAD_SIZE, byteArrayOf(operation) + payload)

        LOGGER.debug {
            """
            |Sent discovery packet:
            |${payloadWithHeader.joinToString()}
            """.trimMargin()
        }

        return server.write(DISCOVERY_PACKET_ID, payloadWithHeader).map {
            LOGGER.debug {
                """
                |Discovery response:
                |${it.joinToString()}
                """.trimMargin()
            }

            it
        }
    }

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
    private fun addResourceGroup(
        resourceIds: List<ResourceId>
    ): IO<Unit> {
        LOGGER.debug {
            """
            |Adding group:
            |resourceIds:
            |${resourceIds.joinToString(separator = "\n\t", prefix = "\t")}
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

        return groupStatus.flatMap {
            if (it[0] == STATUS_ACCEPTED) {
                groupIdToPacketId[groupId] = packetId
                groupIdToCount[groupId] = count

                var currentSendIndex = 0.toByte()
                var currentReceiveIndex = 0.toByte()

                val allResources = IO.fx {
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
                        ).flatMap {
                            if (it[0] == STATUS_ACCEPTED) {
                                groupedResourceToGroupId[resourceId] = groupId
                                groupIdToMembers.getOrPut(groupId) {
                                    mutableSetOf()
                                }.add(resourceId)
                                groupMemberToSendRange[resourceId] = sendStart to sendEnd
                                groupMemberToReceiveRange[resourceId] = receiveStart to receiveEnd

                                currentSendIndex = (currentSendIndex + sendLength).toByte()
                                currentReceiveIndex = (currentReceiveIndex + receiveLength).toByte()

                                IO.just(Unit)
                            } else {
                                IO.raiseError(UnsupportedOperationException(it.joinToString()))
                            }
                        }.bind()
                    }
                }

                allResources.flatMap {
                    IO {
                        // TODO: Expose maxRetries
                        server.addUnreliable(packetId.toByte(), 10)
                    }
                }
            } else {
                IO.raiseError(UnsupportedOperationException(it.joinToString()))
            }
        }
    }

    /**
     * Adds a non-polling resource by sending a discovery packet.
     *
     * @param resourceId The resource id.
     */
    private fun addResource(
        resourceId: ResourceId
    ): IO<Unit> {
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

        return status.flatMap {
            if (it[0] == STATUS_ACCEPTED) {
                // Discovery packet was accepted
                nonGroupedResourceIdToPacketId[resourceId] = packetId

                IO {
                    // TODO: Expose maxRetries
                    server.addUnreliable(packetId.toByte(), 10)
                }
            } else {
                IO.raiseError(UnsupportedOperationException(it.joinToString()))
            }
        }
    }

    /**
     * Computes the next packet id and validates that it can be sent to the device.
     *
     * @return The next packet id.
     */
    private fun getNextPacketId(): Int {
        val id = highestPacketId.getAndIncrement()

        if (isGreaterThanUnsignedByte(id)) {
            throw UnsupportedOperationException("Cannot handle packet ids greater than a byte.")
        }

        return id
    }

    /**
     * Computes the next group id and validates that it can be sent to the device.
     *
     * @return The next group id.
     */
    private fun getNextGroupId(): Int {
        val id = highestGroupId.getAndIncrement()

        if (isGreaterThanUnsignedByte(id)) {
            throw UnsupportedOperationException("Cannot handle group ids greater than a byte.")
        }

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
    ): IO<Int> {
        return getValidatedGroupId(resourcesAndValues.first().first).flatMap { groupId ->
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
                groupIdToMembers[groupId]
            ) {
                return@flatMap IO.raiseError<Int>(
                    UnsupportedOperationException("Mismatched group members.")
                )
            }

            IO.just(groupId)
        }
    }

    /**
     * Validates that all resources are part of the same group, there are the correct number of
     * resources, and all resources in the group are present. Returns the group id if validation
     * succeeded.
     *
     * @param resourceIds The group members.
     * @return The group id.
     */
    private fun validateGroupReceiveResources(resourceIds: List<ResourceId>): IO<Int> {
        return getValidatedGroupId(resourceIds.first()).flatMap { groupId ->
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
    }

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
        return resourcesAndValues.sortedWith(
            Comparator { first, second ->
                groupMemberToSendRange[first.first]!!.first -
                    groupMemberToSendRange[second.first]!!.first
            }
        ).fold(byteArrayOf()) { acc, (a, value) ->
            val sendRange = groupMemberToSendRange[a]!!

            val resourcePayload = makeResourcePayload(value)
            if (resourcePayload.size != sendRange.second - sendRange.first) {
                throw IllegalArgumentException(
                    "The resource payload (${resourcePayload.joinToString()}) " +
                        "was incorrectly sized for the resource $a"
                )
            }

            acc + resourcePayload
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
    ): IO<T> {
        val packetId = getValidatedPacketId(resourceId)
        return server.write(packetId.toByte(), ByteArray(PAYLOAD_SIZE) { 0 }).map {
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
    ): IO<List<T>> {
        return validateGroupReceiveResources(resourceIds).flatMap { groupId ->
            val packetId = groupIdToPacketId[groupId]!!
            server.write(packetId.toByte(), ByteArray(PAYLOAD_SIZE) { 0 }).map {
                parseGroupReceivePayload(
                    resourceIds,
                    it,
                    parseReceivePayload
                )
            }
        }
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
    ): IO<R> {
        val packetId = getValidatedPacketId(resourceId)

        val payload = makeSendPayload(value)
        if (payload.size != resourceId.resourceType.sendLength.toInt()) {
            // TODO: Add a test for this
            throw IllegalArgumentException(
                "The resource payload (${payload.joinToString()}) is " +
                    "incorrectly sized for the resource $resourceId"
            )
        }

        return server.write(packetId.toByte(), getPayload(PAYLOAD_SIZE, payload))
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
    private fun getValidatedPacketId(resourceId: ResourceId): Int {
        return nonGroupedResourceIdToPacketId[resourceId] ?: throw UnsupportedOperationException(
            """
            |The resource id was not discovered:
            |$resourceId
            """.trimMargin()
        )
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
    ): IO<List<R>> {
        return validateGroupSendResources(resourcesAndValues).flatMap { groupId ->
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

    override fun add(resourceId: ResourceId) = addResource(resourceId)

    override fun addGroup(resourceIds: List<ResourceId>) = addResourceGroup(resourceIds)

    override fun isResourceTypeSupported(resourceType: ResourceType): IO<Boolean> {
        TODO("Not yet implemented")
    }

    override fun isResourceInRange(resourceId: ResourceId): IO<Boolean> {
        TODO("Not yet implemented")
    }

    override fun readProtocolVersion(): IO<String> {
        TODO("Not yet implemented")
    }

    protected fun parseGenericReadPayload(payload: ByteArray, start: Int, end: Int) =
        payload.sliceArray(start until end)

    private fun makeGenericWritePayload(payload: ByteArray) = payload

    override fun read(resourceId: ResourceId): IO<ByteArray> {
        return handleRead(resourceId, this::parseGenericReadPayload)
    }

    override fun read(resourceIds: List<ResourceId>): IO<List<ByteArray>> {
        return handleGroupRead(resourceIds, this::parseGenericReadPayload)
    }

    override fun writeAndRead(resourceId: ResourceId, payload: ByteArray): IO<ByteArray> {
        return handleWrite(resourceId, payload, this::makeGenericWritePayload, this::parseGenericReadPayload)
    }

    override fun writeAndRead(resourcesAndValues: List<Pair<ResourceId, ByteArray>>): IO<List<ByteArray>> {
        return handleGroupWrite(resourcesAndValues, this::makeGenericWritePayload, this::parseGenericReadPayload)
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
