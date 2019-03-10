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

/**
 * An implementation of [BowlerRPCProtocol] using SimplePacketComs. Uses a continuous range of
 * packet ids from [getLowestPacketId] through [getHighestPacketId]. Any numbers outside that
 * range are available for other packets.
 *
 * @param comms The comms implementation.
 * @param startPacketId The starting range of the packets this class creates.
 */
@SuppressWarnings("TooManyFunctions")
class SimplePacketComsProtocolOld(
    private val comms: AbstractSimpleComsDevice,
    private val startPacketId: Int = DISCOVERY_PACKET_ID + 1
) : BowlerRPCProtocol {

    private var highestPacketId = AtomicInteger(startPacketId)

    /**
     * All resource ids used in polling reads.
     */
    private val pollingReads = mutableListOf<ResourceId>()

    /**
     * All resource ids used in polling read groups.
     */
    private val pollingReadGroups = mutableListOf<ImmutableSet<ResourceId>>()

    /**
     * ALl resource ids used in reads.
     */
    private val reads = mutableListOf<ResourceId>()

    /**
     * ALl resource ids used in read groups.
     */
    private val readGroups = mutableListOf<ImmutableSet<ResourceId>>()

    /**
     * All resource ids used in writes.
     */
    private val writes = mutableListOf<ResourceId>()

    /**
     * All resource ids used in write groups.
     */
    private val writeGroups = mutableListOf<ImmutableSet<ResourceId>>()

    /**
     * The data for polled read packets.
     */
    private val pollingReadsData = mutableMapOf<Int, Array<Byte>>()

    /**
     * The data for manually sent read packets.
     */
    private val readsData = mutableMapOf<Int, Array<Byte>?>()

    /**
     * The synchronization latches for manually sent read packets.
     */
    private val readsLatches = mutableMapOf<Int, CountDownLatch>()

    /**
     * The data for manually sent write packets.
     */
    private val writesData = mutableMapOf<Int, Array<Byte>?>()

    /**
     * The synchronization latches for manually sent read write.
     */
    private val writesLatches = mutableMapOf<Int, CountDownLatch>()

    /**
     * All the packets that got generated.
     */
    private val packets = mutableMapOf<ResourceId, BytePacketType>()

    /**
     * Whether the device is connected.
     */
    private var isConnected = false

    private val discoveryPacket = BytePacketType(DISCOVERY_PACKET_ID, PACKET_SIZE).apply {
        waitToSendMode()
    }

    private lateinit var discoveryData: Array<Byte>
    private lateinit var discoveryLatch: CountDownLatch

    init {
        require(startPacketId != DISCOVERY_PACKET_ID) {
            "The starting packet id ($startPacketId) cannot be equal to the discovery packet id " +
                "($DISCOVERY_PACKET_ID)."
        }
        comms.addPollingPacket(discoveryPacket)
        comms.addEvent(DISCOVERY_PACKET_ID) {
            discoveryData = comms.readBytes(DISCOVERY_PACKET_ID)
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
     * Sends a discovery packet and waits for the response.
     *
     * @param payload The payload.
     * @return The response payload.
     */
    private fun sendDiscoveryPacket(payload: ByteArray): Array<Byte> {
        validateConnection()
        discoveryLatch = CountDownLatch(1)
        comms.writeBytes(DISCOVERY_PACKET_ID, payload)
        discoveryPacket.oneShotMode()
        discoveryLatch.await()

        println(
            """
            |Discovery response:
            |${discoveryData.joinToString()}
            """.trimMargin()
        )

        return discoveryData
    }

    /**
     * Sends a discovery packet for a new packet (creates the packet on the device side).
     *
     * @param packetId The new packet id.
     * @param payload The payload.
     * @return The response, or [Option.empty] if the discovery packet was not accepted.
     */
    private fun sendDiscoveryForNewPacket(
        operation: Int,
        packetId: Int,
        payload: ByteArray
    ): Option<Array<Byte>> {
        validateConnection()
        val reply = sendDiscoveryPacket(
            listOf(
                operation.toByte(),
                packetId.toByte()
            ).toByteArray() + payload
        )

        val accepted = reply[DISCOVERY_REPLY_STATUS_INDEX].let {
            when (it) {
                DISCOVERY_REPLY_STATUS_ACCEPTED.toByte() -> true
                DISCOVERY_REPLY_STATUS_REJECTED.toByte() -> false
                else -> throw IllegalStateException("Unknown discovery status: $it")
            }
        }

        return if (accepted) {
            Option.just(reply)
        } else {
            Option.empty()
        }
    }

    /**
     * Creates a packet on the device.
     *
     * @param resourceId The resource id.
     * @return The new packet id or an error.
     */
    private fun createPacketOnDevice(resourceId: ResourceId): Either<String, Int> {
        validateConnection()

        val localPacketId = highestPacketId.incrementAndGet()
        val discoveryResponse = sendDiscoveryForNewPacket(
            DISCOVERY_OPERATION_ID, localPacketId, resourceId.bytes
        )

        return if (discoveryResponse.isEmpty()) {
            // Discovery failed
            highestPacketId.decrementAndGet()
            """
            |Discovery failed for resource $resourceId. Packet id at time of discovery:
            |$localPacketId.
            """.trimMargin().left()
        } else {
            localPacketId.right()
        }
    }

    /**
     * Sends a packet and waits for the response. Re-sends the packet on timeout.
     *
     * @param packet The packet to send.
     * @param latches A map of packet id to [CountDownLatch] used to wait for the response.
     * @param data A map of packet id to response payload.
     * @return The response payload.
     */
    private fun reliablySendPacket(
        packet: BytePacketType,
        latches: MutableMap<Int, CountDownLatch>,
        data: MutableMap<Int, Array<Byte>?>
    ): Array<Byte> {
        validateConnection()
        val latch = CountDownLatch(1)

        // Reset the latch
        latches[packet.idOfCommand] = latch

        // Send a new packet
        packet.oneShotMode()

        // Wait for a response
        println("reliablySendPacket waiting")
        latch.await()

        return data[packet.idOfCommand]
            ?: throw IllegalStateException("Packet response was still null after waiting.")
    }

    private fun waitForPollingRead(packetId: Int): Array<Byte> {
        do {
            val data = pollingReadsData[packetId]
            if (data != null) {
                return data
            } else {
                Thread.sleep(1)
            }
        } while (true)
    }

    override fun connect() = Try {
        comms.connect()
        isConnected = true
    }.toEither { it.localizedMessage }.swap().toOption()

    override fun disconnect() {
        comms.disconnect()
        isConnected = false
    }

    override fun addPollingRead(resourceId: ResourceId): Option<String> {
        // Make the packet on the device
        val localPacketId = createPacketOnDevice(resourceId).getOrHandle { return Option.just(it) }

        val packet = BytePacketType(localPacketId, PACKET_SIZE).apply {
            // Put the packet in waitToSendMode so it doesn't send before we writeBytes
            waitToSendMode()
        }

        // Must call allPollingPacket before writeBytes
        comms.addPollingPacket(packet)

        comms.addEvent(localPacketId) {
            println("pollingReads event wrote bytes for $localPacketId")
            pollingReadsData[localPacketId] = comms.readBytes(localPacketId)
        }

        // Polling reads always write the same bytes so just write them once here
        comms.writeBytes(localPacketId, resourceId.bytes)

        // After we call writeBytes then put the packet back into pollingMode
        packet.pollingMode()

        packets[resourceId] = packet
        pollingReads.add(resourceId)

        return Option.empty()
    }

    private fun mapResourceIdToResponseLength(resourceId: ResourceId): Int {
        TODO()
    }

    override fun addPollingReadGroup(resourceIds: ImmutableSet<ResourceId>): Option<String> {
        pollingReadGroups.add(resourceIds)

        /*
        PC Side
        For reads, all the information is packed into one packet
        Send a discovery packet to make a group, specifying the packet id, and length (number of
        elements)
        Send another discovery packet for each resource in the group, specifying the range of
        bytes in the payload (the start byte and the length), and resource

        Device Side
        When we get a discovery packet to make a new group, allocate a vector of the specified
        length in a map of group number to group
        When we get a discovery packet for a group member, index into the map to get the group
        and then push back the group member
         */

        val localPacketId = highestPacketId.incrementAndGet()
        val discoveryResponse = sendDiscoveryForNewPacket(
            GROUP_DISCOVERY_OPERATION_ID,
            localPacketId,
            listOf(resourceIds.size.toByte()).toByteArray()
        )

        if (discoveryResponse.isEmpty()) {
            // Discovery failed
            highestPacketId.decrementAndGet()
            return Option.just(
                """
                |Discovery failed for resources:
                |${resourceIds.joinToString()}
                |Packet id at time of discovery:
                |$localPacketId
                """.trimMargin()
            )
        }

        /**
         * Maps a resource id to the bytes it occupies in the group payload. The first int in the
         * pair is the index of first byte of the read and the second int is the length.
         */
        val resourceIdToByteRange = mutableMapOf<ResourceId, Pair<Int, Int>>()
        var resourceStartingIndex = 0
        resourceIds.forEach {
            val resourceReadLength = mapResourceIdToResponseLength(it)
            resourceIdToByteRange[it] = resourceStartingIndex to resourceReadLength
            resourceStartingIndex += resourceReadLength
        }

        resourceIds.forEach {
            val response = sendDiscoveryForNewPacket(
                GROUP_DISCOVERY_OPERATION_ID,
                localPacketId,
                it.bytes
            )

            if (response.isEmpty()) {
                // Discovery failed
                highestPacketId.decrementAndGet()
                return Option.just(
                    """
                    |Discovery failed for resources:
                    |${resourceIds.joinToString()}
                    |Packet id at time of discovery:
                    |$localPacketId
                    """.trimMargin()
                )
            }
        }

        TODO("not implemented")
    }

    override fun addRead(resourceId: ResourceId): Option<String> {
        // Make the packet on the device
        val localPacketId = createPacketOnDevice(resourceId).getOrHandle { return Option.just(it) }

        val packet = BytePacketType(localPacketId, PACKET_SIZE).apply {
            // Put the packet in waitToSendMode so it doesn't send before we writeBytes. Also,
            // read packets are sent with oneShotMode
            waitToSendMode()
        }

        // Must call allPollingPacket before writeBytes
        comms.addPollingPacket(packet)

        comms.addEvent(localPacketId) {
            readsData[localPacketId] = comms.readBytes(localPacketId)
            readsLatches[localPacketId]?.countDown()
        }

        // TODO: We could get a timeout up to 3 times for the same packet
        // Re-send the packet if it timed out
        comms.addTimeout(localPacketId) { packet.oneShotMode() }

        // Reads always write the same bytes so just write them once here
        comms.writeBytes(localPacketId, resourceId.bytes)

        packets[resourceId] = packet
        reads.add(resourceId)

        return Option.empty()
    }

    override fun addReadGroup(resourceIds: ImmutableSet<ResourceId>): Option<String> {
        readGroups.add(resourceIds)
        TODO("not implemented")
    }

    override fun addWrite(resourceId: ResourceId): Option<String> {
        // Make the packet on the device
        val localPacketId = createPacketOnDevice(resourceId).getOrHandle { return Option.just(it) }

        val packet = BytePacketType(localPacketId, PACKET_SIZE).apply {
            // Put the packet in waitToSendMode so it doesn't send before we writeBytes. Also,
            // write packets are sent with oneShotMode
            waitToSendMode()
        }

        // Must call allPollingPacket before writeBytes
        comms.addPollingPacket(packet)

        comms.addEvent(localPacketId) {
            writesData[localPacketId] = comms.readBytes(localPacketId)
            writesLatches[localPacketId]?.countDown()
        }

        // TODO: We could get a timeout up to 3 times for the same packet
        // Re-send the packet if it timed out
        comms.addTimeout(localPacketId) { packet.oneShotMode() }

        packets[resourceId] = packet
        writes.add(resourceId)

        return Option.empty()
    }

    override fun addWriteGroup(resourceIds: ImmutableSet<ResourceId>): Option<String> {
        writeGroups.add(resourceIds)
        TODO("not implemented")
    }

    override fun isResourceInRange(resourceId: ResourceId): Boolean {
        validateConnection()
        val reply = sendDiscoveryPacket(
            listOf(
                IS_RESOURCE_IN_RANGE_ID.toByte()
            ).toByteArray() + resourceId.bytes
        )

        return reply[DISCOVERY_REPLY_STATUS_INDEX].let {
            when (it) {
                DISCOVERY_REPLY_STATUS_ACCEPTED.toByte() -> true
                DISCOVERY_REPLY_STATUS_REJECTED.toByte() -> false
                else -> throw IllegalStateException("Unknown isResourceInRange status: $it")
            }
        }
    }

    override fun readProtocolVersion(): String {
        validateConnection()
        TODO("not implemented")
    }

    /**
     * Performs a read. If the resource corresponds to a polling read, the latest data from the
     * cache is returned (if the cache has not been filled yet, this method waits). If the
     * resource corresponds to a read, a new packet is sent and this method waits for the
     * response. If the resource does not correspond to either, an error is returned.
     *
     * @param resourceId The resource id.
     * @return The response payload.
     */
    private fun performRead(resourceId: ResourceId): Either<String, Array<Byte>> {
        validateConnection()

        return when {
            // The resource is a polling packet
            pollingReads.contains(resourceId) -> packets[resourceId]?.idOfCommand?.let { id ->
                pollingReadsData[id]?.right() ?: waitForPollingRead(id).right()
            } ?: "Packet was not added.".left()

            // The resource is a non-polling packet
            reads.contains(resourceId) -> packets[resourceId]?.let { packet ->
                // Send a new read packet
                reliablySendPacket(packet, readsLatches, readsData).right()
            } ?: "Packet was not added.".left()

            else -> "Resource id not valid for read: $resourceId".left()
        }
    }

    /**
     * Performs a read by calling [performRead]. If that method returned an error, an
     * [IllegalStateException] is thrown.
     *
     * @param resourceId The resource id.
     * @return The response payload.
     */
    private fun <T> performReadOrFail(
        resourceId: ResourceId,
        parse: (Array<Byte>) -> T
    ): T = performRead(resourceId).fold(
        { throw IllegalStateException(it) },
        { parse(it) }
    )

    /**
     * Performs a write. If the resource corresponds to a write, a new packet is sent and this
     * method waits for the response. If the resource does not correspond to a write, an error is
     * returned.
     *
     * @param resourceId The resource id.
     * @param payload The payload to send.
     * @return The response payload.
     */
    private fun performWrite(
        resourceId: ResourceId,
        payload: ByteArray
    ): Either<String, Array<Byte>> {
        validateConnection()

        return when {
            writes.contains(resourceId) -> packets[resourceId]?.let { packet ->
                comms.writeBytes(packet.idOfCommand, payload)
                // Send a new write packet
                reliablySendPacket(packet, writesLatches, writesData).right()
            } ?: "Packet was not added.".left()

            else -> "Resource id not valid for write: $resourceId".left()
        }
    }

    /**
     * Performs a write by calling [performWrite]. If that method returned an error, an
     * [IllegalStateException] is thrown.
     *
     * @param resourceId The resource id.
     * @param payload The payload to send.
     */
    private fun performWriteOrFail(
        resourceId: ResourceId,
        payload: ByteArray
    ) = performWrite(resourceId, payload).mapLeft { throw IllegalStateException(it) }

    /**
     * Performs a write by calling [performWrite]. If that method returned an error, an
     * [IllegalStateException] is thrown.
     *
     * @param resourceId The resource id.
     * @param payload The payload to send.
     * @return The response payload.
     */
    private fun <T> performWriteOrFail(
        resourceId: ResourceId,
        payload: ByteArray,
        parse: (Array<Byte>) -> T
    ): T = performWrite(resourceId, payload).fold(
        { throw IllegalStateException(it) },
        { parse(it) }
    )

    override fun analogRead(resourceId: ResourceId): Double {
        validateConnection()

        return performReadOrFail(resourceId) {
            it[0].toDouble()
        }
    }

    override fun analogWrite(resourceId: ResourceId, value: Short) {
        validateConnection()

        val buffer = ByteBuffer.allocate(Short.SIZE_BYTES)
        buffer.putShort(value)
        performWriteOrFail(resourceId, buffer.array())
    }

    override fun analogWrite(resourcesAndValues: ImmutableSet<Pair<ResourceId, Short>>) {
        TODO("not implemented")
    }

    override fun buttonRead(resourceId: ResourceId): Boolean {
        validateConnection()

        return performReadOrFail(resourceId) {
            when (it[0]) {
                0.toByte() -> false
                1.toByte() -> true
                else -> throw IllegalStateException("Unknown buttonRead response: $it")
            }
        }
    }

    override fun digitalRead(resourceId: ResourceId): DigitalState {
        validateConnection()

        return performReadOrFail(resourceId) {
            when (it[0]) {
                0.toByte() -> DigitalState.LOW
                1.toByte() -> DigitalState.HIGH
                else -> throw IllegalStateException("Unknown digitalRead response: $it")
            }
        }
    }

    override fun digitalWrite(resourceId: ResourceId, value: DigitalState) {
        validateConnection()

        val buffer = ByteBuffer.allocate(1)
        buffer.put(value.byte)
        performWriteOrFail(resourceId, buffer.array())
    }

    override fun digitalWrite(resourcesAndValues: ImmutableSet<Pair<ResourceId, DigitalState>>) {
        TODO("not implemented")
    }

    override fun encoderRead(resourceId: ResourceId): Long {
        validateConnection()
        TODO("not implemented")
    }

    override fun toneWrite(resourceId: ResourceId, frequency: Int) {
        validateConnection()
        TODO("not implemented")
    }

    override fun toneWrite(resourceId: ResourceId, frequency: Int, duration: Long) {
        validateConnection()
        TODO("not implemented")
    }

    override fun serialWrite(resourceId: ResourceId, message: String) {
        validateConnection()
        TODO("not implemented")
    }

    override fun serialRead(resourceId: ResourceId): String {
        validateConnection()
        TODO("not implemented")
    }

    override fun servoWrite(resourceId: ResourceId, angle: Double) {
        validateConnection()
        TODO("not implemented")
    }

    override fun servoRead(resourceId: ResourceId): Double {
        validateConnection()
        TODO("not implemented")
    }

    override fun ultrasonicRead(resourceId: ResourceId): Long {
        validateConnection()
        TODO("not implemented")
    }

    /**
     * The lowest packet id.
     */
    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getLowestPacketId(): Int = startPacketId

    /**
     * The highest packet id.
     */
    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getHighestPacketId(): Int = highestPacketId.get()

    companion object {
        /**
         * The maximum size of a packet payload in bytes.
         */
        const val PAYLOAD_SIZE = 60

        /**
         * The size of a packet in bytes.
         */
        const val PACKET_SIZE = PAYLOAD_SIZE + 4

        /**
         * The id of the discovery packet.
         */
        const val DISCOVERY_PACKET_ID = 1

        private const val DISCOVERY_OPERATION_ID = 1
        private const val IS_RESOURCE_IN_RANGE_ID = 2
        private const val GROUP_DISCOVERY_OPERATION_ID = 3

        private const val DISCOVERY_REPLY_STATUS_INDEX = 0
        private const val DISCOVERY_REPLY_STATUS_ACCEPTED = 1
        private const val DISCOVERY_REPLY_STATUS_REJECTED = 2
    }
}
