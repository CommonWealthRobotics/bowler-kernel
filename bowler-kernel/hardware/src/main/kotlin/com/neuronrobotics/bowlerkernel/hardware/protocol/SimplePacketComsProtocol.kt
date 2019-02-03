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

import arrow.core.Try
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice
import edu.wpi.SimplePacketComs.BytePacketType
import org.octogonapus.guavautil.collections.toImmutableMap
import java.util.concurrent.CountDownLatch

/**
 * An implementation of [AsyncBowlerRPCProtocol] using SimplePacketComs. Uses a continuous range of
 * packet ids from [getLowestPacketId] through [getHighestPacketId]. Any numbers outside that
 * range are available for other packets.
 *
 * @param comms The comms implementation.
 * @param startPacketId The starting range of the packets this class creates.
 */
@SuppressWarnings("TooManyFunctions")
class SimplePacketComsProtocol(
    private val comms: AbstractSimpleComsDevice,
    private val startPacketId: Int = 1,
    private val pollingReads: ImmutableList<ResourceId>,
    private val reads: ImmutableList<ResourceId>,
    private val writes: ImmutableList<ResourceId>
) : AsyncBowlerRPCProtocol {

    private val highestPacketId = startPacketId + pollingReads.size + reads.size + writes.size

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
     * All the packets that got generated.
     */
    private val packets: ImmutableMap<ResourceId, BytePacketType>

    init {
        var packetId = startPacketId
        val newPackets = mutableMapOf<ResourceId, BytePacketType>()

        pollingReads.forEach {
            val packet = BytePacketType(packetId, PACKET_SIZE)
            newPackets[it] = packet

            comms.addEvent(packetId) { pollingReadsData[packetId] = comms.readBytes(packetId) }
            comms.writeBytes(packetId, it.validatedBytes())
            comms.addPollingPacket(packet)

            packetId++
        }

        reads.forEach {
            val packet = BytePacketType(packetId, PACKET_SIZE)
            packet.oneShotMode()
            newPackets[it] = packet

            comms.addEvent(packetId) {
                readsData[packetId] = comms.readBytes(packetId)
                readsLatches[packetId]?.countDown()
            }

            comms.addTimeout(packetId) {
                readsData[packetId] = null
                readsLatches[packetId]?.countDown()
            }

            comms.addPollingPacket(packet)

            packetId++
        }

        writes.forEach {
            val packet = BytePacketType(packetId, PACKET_SIZE)
            packet.oneShotMode()
            newPackets[it] = packet

            comms.addPollingPacket(packet)

            packetId++
        }

        packets = newPackets.toImmutableMap()
    }

    override fun connect() = Try {
        comms.connect()
    }.toEither { it.localizedMessage }.swap().toOption()

    override fun disconnect() = comms.disconnect()

    override fun isResourceInRange(resourceId: ResourceId): Boolean {
        TODO("not implemented")
    }

    override fun provisionResource(resourceId: ResourceId): Boolean {
        TODO("not implemented")
    }

    override fun readProtocolVersion(): String {
        TODO("not implemented")
    }

    override fun analogRead(resourceId: ResourceId): Double {
        fun Array<Byte>.parse(): Double {
            TODO("Parse the bytes into a double for an analogRead.")
        }

        return packets[resourceId]?.idOfCommand?.let { id ->
            // If the packet is a polling packet, grab the latest data.
            pollingReadsData[id]?.parse() ?: let {
                // Else, read a packet and wait for the response.
                val latch = CountDownLatch(1)

                readsLatches[id] = latch
                comms.writeBytes(id, resourceId.validatedBytes())

                // Wait for a response
                latch.await()

                readsData[id]?.parse()
            } ?: TODO("Packet timed out.")
        } ?: throw UnsupportedOperationException(
            "ResourceId was not added at construction time: $resourceId"
        )
    }

    override fun analogWrite(resourceId: ResourceId, value: Long) {
        TODO("not implemented")
    }

    override fun buttonRead(resourceId: ResourceId): Boolean {
        TODO("not implemented")
    }

    override fun digitalRead(resourceId: ResourceId): DigitalState {
        TODO("not implemented")
    }

    override fun digitalWrite(resourceId: ResourceId, value: DigitalState) {
        TODO("not implemented")
    }

    override fun encoderRead(resourceId: ResourceId): Long {
        TODO("not implemented")
    }

    override fun toneWrite(resourceId: ResourceId, frequency: Long) {
        TODO("not implemented")
    }

    override fun toneWrite(resourceId: ResourceId, frequency: Long, duration: Long) {
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
    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getLowestPacketId() = startPacketId

    /**
     * The highest packet id.
     */
    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getHighestPacketId() = highestPacketId

    /**
     * Get the [ResourceId.bytes] and validate it will fit in a packet.
     */
    private fun ResourceId.validatedBytes() = bytes.also {
        require(it.size <= PAYLOAD_SIZE)
    }

    companion object {
        /**
         * The maximum size of a packet payload in bytes.
         */
        const val PAYLOAD_SIZE = 60

        /**
         * The size of a packet in bytes.
         */
        const val PACKET_SIZE = PAYLOAD_SIZE + 4
    }
}
