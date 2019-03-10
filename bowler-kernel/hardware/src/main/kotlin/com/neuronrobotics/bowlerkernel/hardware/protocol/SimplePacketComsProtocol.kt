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
package com.neuronrobotics.bowlerkernel.hardware.protocol

import arrow.core.Either
import arrow.core.Option
import arrow.core.Try
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice
import edu.wpi.SimplePacketComs.BytePacketType
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
 * @param startPacketId The starting range of the packets this class creates.
 */
@SuppressWarnings("TooManyFunctions")
class SimplePacketComsProtocol(
    private val comms: AbstractSimpleComsDevice,
    private val startPacketId: Int = DISCOVERY_PACKET_ID + 1
) : BowlerRPCProtocol {

    private var highestPacketId = AtomicInteger(startPacketId)
    private var highestGroupId = AtomicInteger(1)

    private val pollingResources = mutableMapOf<ResourceId, Int>()
    private val groupedResourceToGroupId = mutableMapOf<ResourceId, Int>()

    private val nonGroupedResourceIdToPacketId = mutableMapOf<ResourceId, Int>()
    private val packetIdToPacket = mutableMapOf<Int, BytePacketType>()
    private val idToLatch = mutableMapOf<Int, CountDownLatch>()
    private val idToSendData = mutableMapOf<Int, Array<Byte>>()
    private val idToReceiveData = mutableMapOf<Int, Array<Byte>>()

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

    private lateinit var discoveryData: ByteArray
    private lateinit var discoveryLatch: CountDownLatch

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
            discoveryData = comms.readBytes(DISCOVERY_PACKET_ID).toByteArray()
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

        discoveryLatch = CountDownLatch(1)
        comms.writeBytes(DISCOVERY_PACKET_ID, byteArrayOf(operation) + payload)
        discoveryPacket.oneShotMode()
        discoveryLatch.await()

        println(
            """
            |Discovery response:
            |${discoveryData.joinToString()}
            """.trimMargin()
        )

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

    private fun addGroup(
        resourceIds: ImmutableSet<ResourceId>,
        isPolling: Boolean = false,
        configureTimeoutBehavior: (BytePacketType) -> Unit
    ): Option<String> {
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
                idToReceiveData[packetId] = arrayOf()
                idToSendData[packetId] = arrayOf()
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
                    Option.just(
                        """
                        |Failed resource statuses:
                        |${failedResources.joinToString()}
                        """.trimMargin()
                    )
                } else {
                    comms.addPollingPacket(packet)

                    comms.addEvent(packetId) {
                        idToReceiveData[packetId] = comms.readBytes(packetId)
                        idToLatch[packetId]?.countDown()
                    }

                    configureTimeoutBehavior(packet)

                    if (isPolling) {
                        packet.pollingMode()
                    }

                    Option.empty()
                }
            },
            {
                Option.just("Got status: $it")
            }
        )
    }

    /**
     * Adds a non-polling resource.
     *
     * @param resourceId The resource id.
     * @param configureTimeoutBehavior Configures what the packet should do if it times out.
     * @return An error.
     */
    private fun addResource(
        resourceId: ResourceId,
        isPolling: Boolean = false,
        configureTimeoutBehavior: (BytePacketType) -> Unit
    ): Option<String> {
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
                idToReceiveData[packetId] = arrayOf()
                idToSendData[packetId] = arrayOf()

                if (isPolling) {
                    pollingResources[resourceId] = packetId
                }

                comms.addPollingPacket(packet)

                comms.addEvent(packetId) {
                    idToReceiveData[packetId] = comms.readBytes(packetId)
                    idToLatch[packetId]?.countDown()
                }

                configureTimeoutBehavior(packet)

                if (isPolling) {
                    packet.pollingMode()
                }

                Option.empty()
            },
            {
                Option.just("Got status: $it")
            }
        )
    }

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
     * Send an RPC call and wait for the reply.
     *
     * @param id The packet id.
     */
    private fun callAndWait(id: Int) {
        when {
            pollingResources.containsValue(id) -> {
                comms.writeBytes(id, idToSendData[id])
            }

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
     * Validates that all resources are part of the same group and that there are the correct
     * number of resources. Returns the group id if validation succeeded.
     *
     * @param resourcesAndValues The group members and their associated values to send.
     * @return The group id.
     */
    private fun <T> validateGroupResources(
        resourcesAndValues: ImmutableSet<Pair<ResourceId, T>>
    ): Int {
        val groupId = groupedResourceToGroupId[resourcesAndValues.first().first]!!

        require(resourcesAndValues.size == groupIdToCount[groupId])
        require(resourcesAndValues.all { groupedResourceToGroupId[it.first] == groupId })

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
        resourcesAndValues: ImmutableSet<Pair<ResourceId, T>>,
        makeResourcePayload: (T) -> Array<Byte>
    ): Array<Byte> {
        return resourcesAndValues.sortedWith(Comparator { first, second ->
            groupMemberToSendRange[first.first]!!.first -
                groupMemberToSendRange[second.first]!!.first
        }).fold(arrayOf()) { acc, (_, value) ->
            acc + makeResourcePayload(value)
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

    override fun connect() = Try {
        comms.connect()
        isConnected = true
    }.toEither { it.localizedMessage }.swap().toOption()

    override fun disconnect() {
        comms.disconnect()
        isConnected = false
    }

    override fun addPollingRead(resourceId: ResourceId) = addResource(resourceId, true) {}

    override fun addPollingReadGroup(resourceIds: ImmutableSet<ResourceId>) =
        addGroup(resourceIds, true) {
            comms.addTimeout(it.idOfCommand) { it.oneShotMode() }
        }

    override fun addRead(resourceId: ResourceId) = addResource(resourceId) {
        comms.addTimeout(it.idOfCommand) { it.oneShotMode() }
    }

    override fun addReadGroup(resourceIds: ImmutableSet<ResourceId>) = addGroup(resourceIds) {
        comms.addTimeout(it.idOfCommand) { it.oneShotMode() }
    }

    override fun addWrite(resourceId: ResourceId) = addResource(resourceId) {
        comms.addTimeout(it.idOfCommand) { it.oneShotMode() }
    }

    override fun addWriteGroup(resourceIds: ImmutableSet<ResourceId>) = addGroup(resourceIds) {
        comms.addTimeout(it.idOfCommand) { it.oneShotMode() }
    }

    override fun isResourceInRange(resourceId: ResourceId): Boolean = true

    override fun readProtocolVersion(): String {
        TODO("not implemented")
    }

    override fun analogRead(resourceId: ResourceId): Double {
        val id = nonGroupedResourceIdToPacketId[resourceId]!!
        idToSendData[id] = arrayOf()

        callAndWait(id)

        val buffer = ByteBuffer.allocate(2)
        idToReceiveData[id]!!.let {
            buffer.put(it[0])
            buffer.put(it[1])
        }
        buffer.rewind()
        return buffer.char.toDouble()
    }

    private fun makeAnalogWritePayload(value: Short): Array<Byte> {
        val buffer = ByteBuffer.allocate(2)
        buffer.putShort(value)
        return buffer.array().toTypedArray()
    }

    override fun analogWrite(resourceId: ResourceId, value: Short) {
        val id = nonGroupedResourceIdToPacketId[resourceId]!!
        idToSendData[id] = makeAnalogWritePayload(value)
        callAndWait(id)
    }

    override fun analogWrite(resourcesAndValues: ImmutableSet<Pair<ResourceId, Short>>) {
        val groupId = validateGroupResources(resourcesAndValues)
        val packetId = groupIdToPacketId[groupId]!!

        idToSendData[packetId] = makeGroupSendPayload(
            resourcesAndValues,
            this::makeAnalogWritePayload
        )

        callAndWait(packetId)
    }

    override fun buttonRead(resourceId: ResourceId): Boolean {
        TODO("not implemented")
    }

    override fun digitalRead(resourceId: ResourceId): DigitalState {
        TODO("not implemented")
    }

    private fun makeDigitalWritePayload(value: DigitalState): Array<Byte> {
        val buffer = ByteBuffer.allocate(1)
        buffer.put(value.byte)
        return buffer.array().toTypedArray()
    }

    override fun digitalWrite(resourceId: ResourceId, value: DigitalState) {
        val id = nonGroupedResourceIdToPacketId[resourceId]!!
        idToSendData[id] = makeDigitalWritePayload(value)
        callAndWait(id)
    }

    override fun digitalWrite(resourcesAndValues: ImmutableSet<Pair<ResourceId, DigitalState>>) {
        val groupId = validateGroupResources(resourcesAndValues)
        val packetId = groupIdToPacketId[groupId]!!

        idToSendData[packetId] = makeGroupSendPayload(
            resourcesAndValues,
            this::makeDigitalWritePayload
        )

        callAndWait(packetId)
    }

    override fun encoderRead(resourceId: ResourceId): Long {
        TODO("not implemented")
    }

    override fun toneWrite(resourceId: ResourceId, frequency: Int) {
        TODO("not implemented")
    }

    override fun toneWrite(resourceId: ResourceId, frequency: Int, duration: Long) {
        TODO("not implemented")
    }

    override fun serialWrite(resourceId: ResourceId, message: String) {
        TODO("not implemented")
    }

    override fun serialRead(resourceId: ResourceId): String {
        TODO("not implemented")
    }

    override fun servoWrite(resourceId: ResourceId, angle: Double) {
        TODO("not implemented")
    }

    override fun servoRead(resourceId: ResourceId): Double {
        TODO("not implemented")
    }

    override fun ultrasonicRead(resourceId: ResourceId): Long {
        TODO("not implemented")
    }

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
         * The maximum size of a packet payload in bytes.
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
        private const val STATUS_ACCEPTED = 1.toByte()
        private const val STATUS_REJECTED_GENERIC = 2.toByte()
        private const val STATUS_REJECTED_UNKNOWN_RESOURCE = 3.toByte()
        private const val STATUS_REJECTED_UNKNOWN_ATTACHMENT = 4.toByte()
        private const val STATUS_REJECTED_INVALID_ATTACHMENT = 5.toByte()
        private const val STATUS_REJECTED_INVALID_ATTACHMENT_DATA = 6.toByte()
        private const val STATUS_REJECTED_INVALID_GROUP_ID = 7.toByte()
        private const val STATUS_REJECTED_GROUP_FULL = 8.toByte()
        private const val STATUS_REJECTED_UNKNOWN_OPERATION = 9.toByte()
        private const val STATUS_DISCARD_IN_PROGRESS = 10.toByte()
        private const val STATUS_DISCARD_COMPLETE = 11.toByte()

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
