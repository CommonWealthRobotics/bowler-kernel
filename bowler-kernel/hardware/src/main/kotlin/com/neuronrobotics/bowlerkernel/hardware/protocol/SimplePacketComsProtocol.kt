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
import arrow.core.Option
import arrow.core.Try
import arrow.core.flatMap
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceIdValidator
import com.neuronrobotics.bowlerkernel.internal.logging.LoggerUtilities
import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice
import edu.wpi.SimplePacketComs.BytePacketType
import org.octogonapus.ktguava.collections.toImmutableList
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

/**
 * An implementation of [BowlerRPCProtocol] using SimplePacketComs. Uses a continuous range of
 * packet ids from [getLowestPacketId] through [getHighestPacketId]. Any numbers outside that
 * range are available for other packets.
 *
 * @param comms The comms implementation.
 * @param startPacketId The starting id for the packets this class creates.
 * @param resourceIdValidator The resource id validator used to validate resource ids when adding
 * them during discovery.
 */
@SuppressWarnings("TooManyFunctions")
class SimplePacketComsProtocol(
    private val comms: AbstractSimpleComsDevice,
    private val startPacketId: Int = DISCOVERY_PACKET_ID + 1,
    private val resourceIdValidator: ResourceIdValidator
) : BowlerRPCProtocol {

    private var highestPacketId = AtomicInteger(startPacketId)
    private var highestGroupId = AtomicInteger(1)

    private val pollingResources = mutableMapOf<ResourceId, Int>()
    private val groupedResourceToGroupId = mutableMapOf<ResourceId, Int>()
    private val groupIdToMembers = mutableMapOf<Int, MutableSet<ResourceId>>()

    private val nonGroupedResourceIdToPacketId = mutableMapOf<ResourceId, Int>()
    private val packetIdToPacket = mutableMapOf<Int, BytePacketType>()
    private val idToLatch = mutableMapOf<Int, CountDownLatch>()
    private val idToSendData = mutableMapOf<Int, ByteArray>()
    private val idToReceiveData = mutableMapOf<Int, ByteArray>()

    private val groupIdToPacketId = mutableMapOf<Int, Int>()
    private val groupIdToCount = mutableMapOf<Int, Int>()
    private val groupMemberToSendRange = mutableMapOf<ResourceId, Pair<Byte, Byte>>()
    private val groupMemberToReceiveRange = mutableMapOf<ResourceId, Pair<Byte, Byte>>()

    /**
     * Whether the device is connected.
     */
    private var isConnected = false

    private val discoveryPacket = BytePacketType(DISCOVERY_PACKET_ID, PACKET_SIZE).apply {
        waitToSendMode()
    }

    private var discoveryData = ByteArray(PAYLOAD_SIZE) { 0 }
    private var discoveryLatch = CountDownLatch(1)

    init {
        require(startPacketId > 0) {
            "The starting packet id ($startPacketId) must be greater than zero."
        }

        require(startPacketId != DISCOVERY_PACKET_ID) {
            "The starting packet id ($startPacketId) cannot be equal to the discovery packet id " +
                "($DISCOVERY_PACKET_ID)."
        }

        comms.addPollingPacket(discoveryPacket)

        comms.addEvent(DISCOVERY_PACKET_ID) {
            comms.readBytes(DISCOVERY_PACKET_ID, discoveryData)
            discoveryLatch.countDown()
        }

        comms.addTimeout(DISCOVERY_PACKET_ID) { discoveryPacket.oneShotMode() }
    }

    private fun validateConnection() {
        if (!isConnected) {
            throw IllegalStateException("The RPC is not connected.")
        }
    }

    /**
     * Sends a general discovery packet.
     *
     * Section 2.1.1.
     *
     * @param operation The operation.
     * @param payload The payload.
     * @return The payload if accepted, the status code if rejected.
     */
    private fun sendGeneralDiscoveryPacket(
        operation: Byte,
        payload: ByteArray
    ): Either<Byte, ByteArray> {
        validateConnection()

        val payloadWithHeader = byteArrayOf(operation) + payload

        LOGGER.fine {
            """
            |Sent discovery message:
            |${payload.joinToString()}
            """.trimMargin()
        }

        discoveryLatch = CountDownLatch(1)
        comms.writeBytes(DISCOVERY_PACKET_ID, payloadWithHeader)
        discoveryPacket.oneShotMode()
        discoveryLatch.await()

        LOGGER.fine {
            """
            |Discovery response:
            |${discoveryData.joinToString()}
            """.trimMargin()
        }

        val status = discoveryData[0]
        return if (status == STATUS_ACCEPTED) {
            discoveryData.right()
        } else {
            status.left()
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
     * @return [Option.empty] if accepted, the status code if rejected.
     */
    private fun sendDiscoveryPacket(
        packetId: Byte,
        resource: Byte,
        attachment: Byte,
        attachmentData: ByteArray
    ): Option<Byte> =
        sendGeneralDiscoveryPacket(
            OPERATION_DISCOVERY_ID,
            byteArrayOf(packetId, resource, attachment) + attachmentData
        ).swap().toOption()

    /**
     * Sends a group discovery packet.
     *
     * Section 2.1.3.
     *
     * @param groupId The ID for the group being made.
     * @param packetId The ID for the packet the group will use.
     * @param count The number of resources that will be added to the group.
     * @return [Option.empty] if accepted, the status code if rejected.
     */
    private fun sendGroupDiscoveryPacket(
        groupId: Byte,
        packetId: Byte,
        count: Byte
    ): Option<Byte> =
        sendGeneralDiscoveryPacket(
            OPERATION_GROUP_DISCOVERY_ID,
            byteArrayOf(groupId, packetId, count)
        ).swap().toOption()

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
     * @return [Option.empty] if accepted, the status code if rejected.
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
    ): Option<Byte> =
        sendGeneralDiscoveryPacket(
            OPERATION_GROUP_MEMBER_DISCOVERY_ID,
            byteArrayOf(
                groupId, sendStart, sendEnd, receiveStart, receiveEnd, resource, attachment
            ) + attachmentData
        ).swap().toOption()

    /**
     * Sends a discard discovery packet.
     *
     * Section 2.1.5.
     *
     * @return The status code.
     */
    private fun sendDiscardDiscoveryPacket(): Byte =
        sendGeneralDiscoveryPacket(
            OPERATION_DISCARD_DISCOVERY_ID, byteArrayOf()
        ).swap().getOrHandle { it[0] }

    /**
     * Adds a group by sending a group discovery packet followed by a group member discovery
     * packet for each element of [resourceIds]. Does not fail-fast when discovering group members.
     *
     * @param resourceIds The group members.
     * @param isPolling Whether the group packet is polling.
     * @param configureTimeoutBehavior Called with the newly created packet. Should configure any
     * packet timeout behavior. This method does not get a timeout event handler on its own.
     * @return An error.
     */
    private fun addGroup(
        resourceIds: ImmutableSet<ResourceId>,
        isPolling: Boolean,
        configureTimeoutBehavior: (BytePacketType) -> Unit
    ): Either<String, Unit> {
        val groupId = getNextGroupId()
        val packetId = getNextPacketId()
        val count = resourceIds.size

        val groupStatus =
            sendGroupDiscoveryPacket(groupId.toByte(), packetId.toByte(), count.toByte())

        return groupStatus.fold(
            {
                val packet = BytePacketType(packetId, PACKET_SIZE).apply {
                    waitToSendMode()
                }

                packetIdToPacket[packetId] = packet
                idToReceiveData[packetId] = ByteArray(PAYLOAD_SIZE) { 0 }
                idToSendData[packetId] = ByteArray(PAYLOAD_SIZE) { 0 }
                groupIdToPacketId[groupId] = packetId
                groupIdToCount[groupId] = count

                var currentSendIndex = 0.toByte()
                var currentReceiveIndex = 0.toByte()

                val failedResources = resourceIds.map { resourceId ->
                    val sendLength = resourceId.resourceType.sendLength
                    val receiveLength = resourceId.resourceType.receiveLength

                    if (isGreaterThanUnsignedByte(currentSendIndex + sendLength) ||
                        isGreaterThanUnsignedByte(currentReceiveIndex + receiveLength)
                    ) {
                        throw IllegalStateException(
                            "Cannot handle payload indices greater than a byte."
                        )
                    }

                    val sendStart = currentSendIndex
                    val sendEnd = (currentSendIndex + sendLength).toByte()
                    val receiveStart = currentReceiveIndex
                    val receiveEnd = (currentReceiveIndex + receiveLength).toByte()

                    val status = sendGroupMemberDiscoveryPacket(
                        groupId.toByte(),
                        sendStart,
                        sendEnd,
                        receiveStart,
                        receiveEnd,
                        resourceId.resourceType.type,
                        resourceId.attachmentPoint.type,
                        resourceId.attachmentPoint.data
                    )

                    status.fold(
                        {
                            groupedResourceToGroupId[resourceId] = groupId
                            groupIdToMembers.getOrPut(groupId) { mutableSetOf() }.add(resourceId)
                            groupMemberToSendRange[resourceId] = sendStart to sendEnd
                            groupMemberToReceiveRange[resourceId] = receiveStart to receiveEnd

                            currentSendIndex = (currentSendIndex + sendLength).toByte()
                            currentReceiveIndex = (currentReceiveIndex + receiveLength).toByte()

                            Option.empty<String>()
                        },
                        {
                            Option.just("Got status: $it")
                        }
                    )
                }.filter { it.nonEmpty() }

                if (failedResources.isNotEmpty()) {
                    """
                        |Failed resource statuses:
                        |${failedResources.joinToString()}
                        """.trimMargin().left()
                } else {
                    comms.addPollingPacket(packet)

                    comms.addEvent(packetId) {
                        comms.readBytes(packetId, idToReceiveData[packetId])
                        idToLatch[packetId]?.countDown()
                    }

                    configureTimeoutBehavior(packet)

                    if (isPolling) {
                        packet.pollingMode()
                    }

                    Unit.right()
                }
            },
            {
                "Got status: $it".left()
            }
        )
    }

    /**
     * Adds a non-polling resource by sending a discovery packet.
     *
     * @param resourceId The resource id.
     * @param configureTimeoutBehavior Called with the newly created packet. Should configure any
     * packet timeout behavior. This method does not get a timeout event handler on its own.
     * @return An error.
     */
    private fun addResource(
        resourceId: ResourceId,
        isPolling: Boolean = false,
        configureTimeoutBehavior: (BytePacketType) -> Unit
    ): Either<String, Unit> {
        val packetId = getNextPacketId()

        val status = sendDiscoveryPacket(
            packetId.toByte(),
            resourceId.resourceType.type,
            resourceId.attachmentPoint.type,
            resourceId.attachmentPoint.data
        )

        return status.fold(
            {
                val packet = BytePacketType(packetId, PACKET_SIZE).apply {
                    waitToSendMode()
                }

                // Discovery packet was accepted
                nonGroupedResourceIdToPacketId[resourceId] = packetId
                packetIdToPacket[packetId] = packet
                idToReceiveData[packetId] = ByteArray(PAYLOAD_SIZE) { 0 }
                idToSendData[packetId] = ByteArray(PAYLOAD_SIZE) { 0 }

                if (isPolling) {
                    pollingResources[resourceId] = packetId
                }

                comms.addPollingPacket(packet)

                comms.addEvent(packetId) {
                    comms.readBytes(packetId, idToReceiveData[packetId])
                    idToLatch[packetId]?.countDown()
                }

                configureTimeoutBehavior(packet)

                if (isPolling) {
                    packet.pollingMode()
                }

                Unit.right()
            },
            {
                "Got status: $it".left()
            }
        )
    }

    /**
     * Computes the next packet id and validates that it can be sent to the device.
     *
     * @return The next packet id.
     */
    private fun getNextPacketId(): Int {
        val id = highestPacketId.getAndIncrement()

        if (isGreaterThanUnsignedByte(id)) {
            throw IllegalStateException("Cannot handle packet ids greater than a byte.")
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
            throw IllegalStateException("Cannot handle group ids greater than a byte.")
        }

        return id
    }

    /**
     * Sends an RPC call. If the [id] corresponds to a polling packet, this method does not wait.
     * If it does not correspond to a polling packet, this method waits for the response. This
     * method does not touch packet timeout behavior.
     *
     * @param id The packet id.
     */
    private fun callAndWait(id: Int) {
        when {
            pollingResources.containsValue(id) -> comms.writeBytes(id, idToSendData[id])

            else -> {
                val latch = CountDownLatch(1)
                idToLatch[id] = latch

                comms.writeBytes(id, idToSendData[id])
                packetIdToPacket[id]!!.oneShotMode()

                latch.await()
            }
        }
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
        resourcesAndValues: ImmutableList<Pair<ResourceId, T>>
    ): Int {
        val groupId = groupedResourceToGroupId[resourcesAndValues.first().first]!!

        require(resourcesAndValues.size == groupIdToCount[groupId])
        require(resourcesAndValues.all { groupedResourceToGroupId[it.first] == groupId })
        require(
            resourcesAndValues.mapTo(LinkedHashSet(resourcesAndValues.size)) { it.first }
                == groupIdToMembers[groupId]
        )

        return groupId
    }

    /**
     * Validates that all resources are part of the same group, there are the correct number of
     * resources, and all resources in the group are present. Returns the group id if validation
     * succeeded.
     *
     * @param resourceIds The group members.
     * @return The group id.
     */
    private fun validateGroupReceiveResources(resourceIds: ImmutableList<ResourceId>): Int {
        val groupId = groupedResourceToGroupId[resourceIds.first()]!!

        require(resourceIds.size == groupIdToCount[groupId])
        require(resourceIds.all { groupedResourceToGroupId[it] == groupId })
        require(resourceIds.toSet() == groupIdToMembers[groupId])

        return groupId
    }

    /**
     * Creates the send payload for a group.
     *
     * @param resourcesAndValues The group members and their associated values to send.
     * @param makeResourcePayload Creates the payload for one resource given its value.
     * @return The payload.
     */
    private fun <T> makeGroupSendPayload(
        resourcesAndValues: ImmutableList<Pair<ResourceId, T>>,
        makeResourcePayload: (T) -> ByteArray
    ): ByteArray {
        return resourcesAndValues.sortedWith(Comparator { first, second ->
            groupMemberToSendRange[first.first]!!.first -
                groupMemberToSendRange[second.first]!!.first
        }).fold(byteArrayOf()) { acc, (_, value) ->
            acc + makeResourcePayload(value)
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
        resourceIds: ImmutableList<ResourceId>,
        fullPayload: ByteArray,
        parseResourcePayload: (ByteArray, Int, Int) -> T
    ): ImmutableList<T> {
        return resourceIds.map {
            val receiveRange = groupMemberToReceiveRange[it]!!
            parseResourcePayload(
                fullPayload,
                receiveRange.first.toInt(),
                receiveRange.second.toInt()
            )
        }.toImmutableList()
    }

    /**
     * Performs a full RPC read call:
     * 1. Get packet id
     * 2. Write to [idToSendData]
     * 3. Call [callAndWait]
     * 4. Call [parseReceivePayload] and return the result
     *
     * @param resourceId The resource id.
     * @param parseReceivePayload Parses the receive payload into a value.
     * @return The value.
     */
    private fun <T> handleRead(
        resourceId: ResourceId,
        parseReceivePayload: (ByteArray, Int, Int) -> T
    ): T {
        val packetId = nonGroupedResourceIdToPacketId[resourceId]!!

        idToSendData[packetId] = ByteArray(PAYLOAD_SIZE) { 0 }
        callAndWait(packetId)

        return idToReceiveData[packetId]!!.let {
            parseReceivePayload(it, 0, it.size)
        }
    }

    /**
     * Performs a full RPC group read call:
     * 1. Validate group and get group id
     * 2. Get packet id
     * 3. Write to [idToSendData]
     * 4. Call [callAndWait]
     * 5. Call [parseGroupReceivePayload] with [parseReceivePayload] and return the result
     *
     * @param resourceIds The group members.
     * @param parseReceivePayload Parses a member's section of the group receive payload.
     * @return The values in the same order as [resourceIds].
     */
    private fun <T> handleGroupRead(
        resourceIds: ImmutableList<ResourceId>,
        parseReceivePayload: (ByteArray, Int, Int) -> T
    ): ImmutableList<T> {
        val groupId = validateGroupReceiveResources(resourceIds)
        val packetId = groupIdToPacketId[groupId]!!

        idToSendData[packetId] = ByteArray(PAYLOAD_SIZE) { 0 }
        callAndWait(packetId)

        return parseGroupReceivePayload(
            resourceIds,
            idToReceiveData[packetId]!!,
            parseReceivePayload
        )
    }

    /**
     * Performs a full RPC write call:
     * 1. Get packet id
     * 2. Write to [idToSendData]
     * 3. Call [callAndWait]
     *
     * @param resourceId The resource id.
     * @param value The value to write.
     * @param makeSendPayload Makes a send payload from a value.
     */
    private fun <T> handleWrite(
        resourceId: ResourceId,
        value: T,
        makeSendPayload: (T) -> ByteArray
    ) {
        val packetId = nonGroupedResourceIdToPacketId[resourceId]!!
        idToSendData[packetId] = makeSendPayload(value)
        callAndWait(packetId)
    }

    /**
     * Performs a full RPC group write call:
     * 1. Validate group and get group id
     * 2. Get packet id
     * 3. Call [makeGroupSendPayload] with [makeSendPayload] and write the result to [idToSendData]
     * 4. Call [callAndWait]
     */
    private fun <T> handleGroupWrite(
        resourcesAndValues: ImmutableList<Pair<ResourceId, T>>,
        makeSendPayload: (T) -> ByteArray
    ) {
        val groupId = validateGroupSendResources(resourcesAndValues)
        val packetId = groupIdToPacketId[groupId]!!

        idToSendData[packetId] = makeGroupSendPayload(
            resourcesAndValues,
            makeSendPayload
        )

        callAndWait(packetId)
    }

    override fun connect(): Either<String, Unit> = Try {
        comms.connect()
        isConnected = true
    }.toEither { it.localizedMessage }

    override fun disconnect(): Either<String, Unit> {
        var status = sendDiscardDiscoveryPacket()

        // Wait for the discard operation to complete
        while (status == STATUS_DISCARD_IN_PROGRESS) {
            status = sendDiscardDiscoveryPacket()
            Thread.sleep(100)
        }

        comms.disconnect()
        isConnected = false

        return if (status == STATUS_DISCARD_COMPLETE) {
            Unit.right()
        } else {
            "Got status code while trying to disconnect: $status".left()
        }
    }

    override fun addPollingRead(resourceId: ResourceId) =
        resourceIdValidator.validateIsReadType(resourceId).flatMap {
            addResource(resourceId, true) { it.oneShotMode() }
        }

    override fun addPollingReadGroup(resourceIds: ImmutableSet<ResourceId>): Either<String, Unit> {
        val invalidResources =
            validateResources(resourceIds, ResourceIdValidator::validateIsReadType)

        return if (invalidResources.isNotEmpty()) {
            """
            |Found invalid resources when trying to add a polling read group:
            |${invalidResources.joinToString("\n")}
            """.trimMargin().left()
        } else {
            addGroup(resourceIds, true) {
                comms.addTimeout(it.idOfCommand) { it.oneShotMode() }
            }
        }
    }

    override fun addRead(resourceId: ResourceId) =
        resourceIdValidator.validateIsReadType(resourceId).flatMap {
            addResource(resourceId) {
                comms.addTimeout(it.idOfCommand) { it.oneShotMode() }
            }
        }

    override fun addReadGroup(resourceIds: ImmutableSet<ResourceId>): Either<String, Unit> {
        val invalidResources =
            validateResources(resourceIds, ResourceIdValidator::validateIsReadType)

        return if (invalidResources.isNotEmpty()) {
            """
            |Found invalid resources when trying to add a read group:
            |${invalidResources.joinToString("\n")}
            """.trimMargin().left()
        } else {
            addGroup(resourceIds, false) {
                comms.addTimeout(it.idOfCommand) { it.oneShotMode() }
            }
        }
    }

    override fun addWrite(resourceId: ResourceId) =
        resourceIdValidator.validateIsWriteType(resourceId).flatMap {
            addResource(resourceId) {
                comms.addTimeout(it.idOfCommand) { it.oneShotMode() }
            }
        }

    override fun addWriteGroup(resourceIds: ImmutableSet<ResourceId>): Either<String, Unit> {
        val invalidResources =
            validateResources(resourceIds, ResourceIdValidator::validateIsWriteType)

        return if (invalidResources.isNotEmpty()) {
            """
            |Found invalid resources when trying to add a write group:
            |${invalidResources.joinToString("\n")}
            """.trimMargin().left()
        } else {
            addGroup(resourceIds, false) {
                comms.addTimeout(it.idOfCommand) { it.oneShotMode() }
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
        validator: ResourceIdValidator.(ResourceId) -> Either<String, Unit>
    ): List<Either<String, Unit>> {
        return resourceIds.map {
            resourceIdValidator.validator(it)
        }.filter {
            it.isLeft()
        }
    }

    override fun isResourceInRange(resourceId: ResourceId): Boolean = true

    override fun readProtocolVersion(): String {
        TODO("not implemented")
    }

    @Suppress("UNUSED_PARAMETER")
    private fun parseAnalogReadPayload(payload: ByteArray, start: Int, end: Int): Double {
        val buffer = ByteBuffer.allocate(2)
        buffer.put(payload[start])
        buffer.put(payload[start + 1])
        buffer.rewind()
        return buffer.char.toDouble()
    }

    override fun analogRead(resourceId: ResourceId) =
        handleRead(resourceId, this::parseAnalogReadPayload)

    override fun analogRead(resourceIds: ImmutableList<ResourceId>) =
        handleGroupRead(resourceIds, this::parseAnalogReadPayload)

    private fun makeAnalogWritePayload(value: Short): ByteArray {
        val buffer = ByteBuffer.allocate(2)
        buffer.putShort(value)
        return buffer.array()
    }

    override fun analogWrite(resourceId: ResourceId, value: Short) =
        handleWrite(resourceId, value, this::makeAnalogWritePayload)

    override fun analogWrite(resourcesAndValues: ImmutableList<Pair<ResourceId, Short>>) =
        handleGroupWrite(resourcesAndValues, this::makeAnalogWritePayload)

    @Suppress("UNUSED_PARAMETER")
    private fun parseButtonReadPayload(payload: ByteArray, start: Int, end: Int): Boolean =
        payload[start] == 0.toByte()

    override fun buttonRead(resourceId: ResourceId) =
        handleRead(resourceId, this::parseButtonReadPayload)

    override fun buttonRead(resourceIds: ImmutableList<ResourceId>) =
        handleGroupRead(resourceIds, this::parseButtonReadPayload)

    @Suppress("UNUSED_PARAMETER")
    private fun parseDigitalReadPayload(payload: ByteArray, start: Int, end: Int): DigitalState {
        return if (payload[start] == 0.toByte()) {
            DigitalState.LOW
        } else {
            DigitalState.HIGH
        }
    }

    override fun digitalRead(resourceId: ResourceId) =
        handleRead(resourceId, this::parseDigitalReadPayload)

    override fun digitalRead(resourceIds: ImmutableList<ResourceId>) =
        handleGroupRead(resourceIds, this::parseDigitalReadPayload)

    private fun makeDigitalWritePayload(value: DigitalState): ByteArray {
        val buffer = ByteBuffer.allocate(1)
        buffer.put(value.byte)
        return buffer.array()
    }

    override fun digitalWrite(resourceId: ResourceId, value: DigitalState) =
        handleWrite(resourceId, value, this::makeDigitalWritePayload)

    override fun digitalWrite(resourcesAndValues: ImmutableList<Pair<ResourceId, DigitalState>>) =
        handleGroupWrite(resourcesAndValues, this::makeDigitalWritePayload)

    @Suppress("UNUSED_PARAMETER")
    private fun parseEncoderReadPayload(payload: ByteArray, start: Int, end: Int): Long {
        TODO()
    }

    override fun encoderRead(resourceId: ResourceId) =
        handleRead(resourceId, this::parseEncoderReadPayload)

    override fun encoderRead(resourceIds: ImmutableList<ResourceId>) =
        handleGroupRead(resourceIds, this::parseEncoderReadPayload)

    @Suppress("UNUSED_PARAMETER")
    private fun makeToneWritePayload(frequencyAndDuration: Pair<Int, Long>): ByteArray {
        TODO()
    }

    override fun toneWrite(resourceId: ResourceId, frequency: Int) =
        handleWrite(resourceId, frequency to (-1).toLong(), this::makeToneWritePayload)

    override fun toneWrite(resourceId: ResourceId, frequency: Int, duration: Long) =
        handleWrite(resourceId, frequency to duration, this::makeToneWritePayload)

    @Suppress("UNUSED_PARAMETER")
    private fun makeSerialWritePayload(message: String): ByteArray {
        TODO()
    }

    override fun serialWrite(resourceId: ResourceId, message: String) =
        handleWrite(resourceId, message, this::makeSerialWritePayload)

    @Suppress("UNUSED_PARAMETER")
    private fun parseSerialReadPayload(payload: ByteArray, start: Int, end: Int): String {
        TODO()
    }

    override fun serialRead(resourceId: ResourceId) =
        handleRead(resourceId, this::parseSerialReadPayload)

    @Suppress("UNUSED_PARAMETER")
    private fun makeServoWritePayload(angle: Double): ByteArray {
        TODO()
    }

    override fun servoWrite(resourceId: ResourceId, angle: Double) =
        handleWrite(resourceId, angle, this::makeServoWritePayload)

    override fun servoWrite(resourcesAndValues: ImmutableList<Pair<ResourceId, Double>>) =
        handleGroupWrite(resourcesAndValues, this::makeServoWritePayload)

    @Suppress("UNUSED_PARAMETER")
    private fun parseServoReadPayload(payload: ByteArray, start: Int, end: Int): Double {
        TODO()
    }

    override fun servoRead(resourceId: ResourceId) =
        handleRead(resourceId, this::parseServoReadPayload)

    override fun servoRead(resourceIds: ImmutableList<ResourceId>) =
        handleGroupRead(resourceIds, this::parseServoReadPayload)

    @Suppress("UNUSED_PARAMETER")
    private fun parseUltrasonicReadPayload(payload: ByteArray, start: Int, end: Int): Long {
        TODO()
    }

    override fun ultrasonicRead(resourceId: ResourceId) =
        handleRead(resourceId, this::parseUltrasonicReadPayload)

    override fun ultrasonicRead(resourceIds: ImmutableList<ResourceId>) =
        handleGroupRead(resourceIds, this::parseUltrasonicReadPayload)

    /**
     * The lowest packet id.
     */
    fun getLowestPacketId(): Int = startPacketId

    /**
     * The highest packet id.
     */
    fun getHighestPacketId(): Int = highestPacketId.get()

    companion object {

        /**
         * The id of the discovery packet.
         */
        const val DISCOVERY_PACKET_ID = 1

        /**
         * The size of a payload in bytes.
         */
        const val PAYLOAD_SIZE = 60

        /**
         * The size of a packet in bytes.
         */
        const val PACKET_SIZE = PAYLOAD_SIZE + 4

        /**
         * The operation ID's.
         */
        private const val OPERATION_DISCOVERY_ID = 1.toByte()
        private const val OPERATION_GROUP_DISCOVERY_ID = 2.toByte()
        private const val OPERATION_GROUP_MEMBER_DISCOVERY_ID = 3.toByte()
        private const val OPERATION_DISCARD_DISCOVERY_ID = 4.toByte()

        /**
         * The status codes.
         */
        const val STATUS_ACCEPTED = 1.toByte()
        const val STATUS_REJECTED_GENERIC = 2.toByte()
        const val STATUS_REJECTED_UNKNOWN_RESOURCE = 3.toByte()
        const val STATUS_REJECTED_UNKNOWN_ATTACHMENT = 4.toByte()
        const val STATUS_REJECTED_INVALID_ATTACHMENT = 5.toByte()
        const val STATUS_REJECTED_INVALID_ATTACHMENT_DATA = 6.toByte()
        const val STATUS_REJECTED_INVALID_GROUP_ID = 7.toByte()
        const val STATUS_REJECTED_GROUP_FULL = 8.toByte()
        const val STATUS_REJECTED_UNKNOWN_OPERATION = 9.toByte()
        const val STATUS_DISCARD_IN_PROGRESS = 10.toByte()
        const val STATUS_DISCARD_COMPLETE = 11.toByte()

        private val LOGGER =
            LoggerUtilities.getLogger(SimplePacketComsProtocol::class.java.simpleName)

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
