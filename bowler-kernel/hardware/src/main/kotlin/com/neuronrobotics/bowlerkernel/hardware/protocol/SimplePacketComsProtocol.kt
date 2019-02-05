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
    private val packets: ImmutableMap<ResourceId, BytePacketType>

    init {
        var packetId = startPacketId
        val newPackets = mutableMapOf<ResourceId, BytePacketType>()

        pollingReads.forEach {
            val localPacketId = packetId
            val packet = BytePacketType(localPacketId, PACKET_SIZE)

            newPackets[it] = packet

            comms.addPollingPacket(packet)

            comms.addEvent(localPacketId) {
                println("pollingReads event wrote bytes for $localPacketId")
                pollingReadsData[localPacketId] = comms.readBytes(localPacketId)
            }

            comms.writeBytes(localPacketId, it.validatedBytes())

            packetId++
        }

        reads.forEach {
            val localPacketId = packetId
            val packet = BytePacketType(localPacketId, PACKET_SIZE).apply {
                waitToSendMode()
            }

            newPackets[it] = packet

            comms.addPollingPacket(packet)

            comms.addEvent(localPacketId) {
                readsData[localPacketId] = comms.readBytes(localPacketId)
                readsLatches[localPacketId]?.countDown()
            }

            // TODO: We could get a timeout up to 3 times for the same packet
            comms.addTimeout(localPacketId) {
                packet.oneShotMode()
            }

            comms.writeBytes(localPacketId, it.validatedBytes())

            packetId++
        }

        writes.forEach {
            val localPacketId = packetId
            val packet = BytePacketType(localPacketId, PACKET_SIZE).apply {
                waitToSendMode()
            }

            newPackets[it] = packet

            comms.addPollingPacket(packet)

            comms.addEvent(localPacketId) {
                writesData[localPacketId] = comms.readBytes(localPacketId)
                writesLatches[localPacketId]?.countDown()
            }

            // TODO: We could get a timeout up to 3 times for the same packet
            comms.addTimeout(localPacketId) {
                packet.oneShotMode()
            }

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

    /**
     * Sends a packet and waits for the response. Re-sends the packet on timeout.
     *
     * @param packet The packet to send.
     * @param latches A map of packet id to [CountDownLatch] used to wait for the response.
     * @param data A map of packet id to response payload.
     * @param parse The function to parse the response packet.
     * @return The response.
     */
    private fun <T> reliablySendPacket(
        packet: BytePacketType,
        latches: MutableMap<Int, CountDownLatch>,
        data: MutableMap<Int, Array<Byte>?>,
        parse: Array<Byte>.() -> T
    ): T {
        val latch = CountDownLatch(1)

        // Reset the latch
        latches[packet.idOfCommand] = latch

        // Send a new packet
        packet.oneShotMode()

        // Wait for a response
        println("reliablySendPacket waiting")
        latch.await()

        return data[packet.idOfCommand]?.parse()
            ?: throw IllegalStateException("Packet response was still null after waiting.")
    }

    /**
     * Try to send a read packet. Re-sends the packet on timeout.
     *
     * @param packet The packet to send.
     * @param parse The function to parse the response packet.
     * @return The response.
     */
    private fun <T> tryToSendRead(
        packet: BytePacketType,
        parse: Array<Byte>.() -> T
    ): T = reliablySendPacket(packet, readsLatches, readsData, parse)

    /**
     * Try to send a write packet. Re-sends the packet on timeout.
     *
     * @param packet The packet to send.
     * @param parse The function to parse the response packet.
     * @return The response.
     */
    private fun <T> tryToSendWrite(
        packet: BytePacketType,
        parse: Array<Byte>.() -> T
    ): T = reliablySendPacket(packet, writesLatches, writesData, parse)

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

    override fun analogRead(resourceId: ResourceId): Double {
        fun Array<Byte>.parse(): Double {
            println(joinToString())
            return this[4].toDouble()
        }

        return when {
            // The resource is a polling packet
            pollingReads.contains(resourceId) -> packets[resourceId]?.idOfCommand?.let { id ->
                pollingReadsData[id]?.parse() ?: waitForPollingRead(id).parse()
            } ?: throw IllegalStateException("Unknown error.")

            // The resource is a non-polling packet
            reads.contains(resourceId) -> packets[resourceId]?.let { packet ->
                // Send a new read packet
                tryToSendRead(packet) { parse() }
            } ?: throw IllegalStateException("Unknown error.")

            else -> throw IllegalArgumentException("Resource id not valid for read: $resourceId")
        }
    }

    override fun analogWrite(resourceId: ResourceId, value: Long) {
        fun Array<Byte>.parse(): Double {
            println(joinToString())
            return this[4].toDouble()
        }

        when {
            writes.contains(resourceId) -> packets[resourceId]?.let { packet ->
                // Send a new read packet
                comms.writeBytes(
                    packet.idOfCommand,
                    resourceId.validatedBytes() + listOf(value).map { it.toByte() }.toByteArray()
                )
                println(tryToSendWrite(packet) { parse() })
            } ?: throw IllegalStateException("Unknown error.")

            else -> throw IllegalArgumentException("Resource id not valid for write: $resourceId")
        }
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
