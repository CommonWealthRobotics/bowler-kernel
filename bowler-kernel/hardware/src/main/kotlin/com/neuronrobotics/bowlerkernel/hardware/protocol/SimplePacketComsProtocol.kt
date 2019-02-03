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
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice
import edu.wpi.SimplePacketComs.BytePacketType
import edu.wpi.SimplePacketComs.FloatPacketType
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger

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
    private val startPacketId: Int = 1
) : AsyncBowlerRPCProtocol {

    private val isResourceInRangePacket = BytePacketType(startPacketId, PACKET_SIZE)
    private val provisionResourcePacket = BytePacketType(startPacketId + 1, PACKET_SIZE)
    private val readProtocolVersionPacket = BytePacketType(startPacketId + 2, PACKET_SIZE)
    private val analogReadPacket = FloatPacketType(startPacketId + 3, PACKET_SIZE)
    private val analogWritePacket = BytePacketType(startPacketId + 4, PACKET_SIZE)
    private val buttonReadPacket = BytePacketType(startPacketId + 5, PACKET_SIZE)
    private val digitalReadPacket = BytePacketType(startPacketId + 6, PACKET_SIZE)
    private val digitalWritePacket = BytePacketType(startPacketId + 7, PACKET_SIZE)
    private val encoderReadPacket = BytePacketType(startPacketId + 8, PACKET_SIZE)
    private val toneWritePacket = FloatPacketType(startPacketId + 9, PACKET_SIZE)
    private val serialWritePacket = BytePacketType(startPacketId + 10, PACKET_SIZE)
    private val serialReadPacket = BytePacketType(startPacketId + 11, PACKET_SIZE)
    private val servoWritePacket = FloatPacketType(startPacketId + 12, PACKET_SIZE)
    private val servoReadPacket = FloatPacketType(startPacketId + 13, PACKET_SIZE)
    private val ultrasonicReadPacket = FloatPacketType(startPacketId + 14, PACKET_SIZE)

    /**
     * A counter used to distinguish between RPC calls. Each RPC call uses the same packet id, so
     * this counter is used to distinguish between different instances of the call.
     */
    private val packetCounter = AtomicInteger(0)

    /**
     * An array of byte buffers to convert the packet count int into bytes.
     */
    private val writeByteBuffers = Array<ByteBuffer>(
        (getHighestPacketId() - getLowestPacketId()) + 1
    ) { ByteBuffer.allocate(4) }

    /**
     * An array of byte buffers to convert the packet count bytes into an int.
     */
    private val readByteBuffers = Array<ByteBuffer>(
        (getHighestPacketId() - getLowestPacketId()) + 1
    ) { ByteBuffer.allocate(4) }

    /**
     * A mutable map of the timeout and success callbacks for each packet.
     */
    private val packetCallbacks: MutableMap<Int, Pair<() -> Unit, (ByteArray) -> Unit>> =
        mutableMapOf()

    init {
        comms.addEvent(isResourceInRangePacket.idOfCommand) {
            val bytes = comms.readBytes(isResourceInRangePacket.idOfCommand).toByteArray()
            val buffer = readByteBuffers[isResourceInRangePacket.idOfCommand - startPacketId]

            // The first 4 bytes of any packet is the packet count
            buffer.put(bytes, 0, 4)

            packetCallbacks.remove(buffer.int)?.second?.invoke(bytes)
        }

        comms.addTimeout(isResourceInRangePacket.idOfCommand) {
            // TODO: We need to recover the packet count somehow so we can index into the map:
            // packetCallbacks.remove(<packet count here>)?.first?.invoke()
        }
    }

    override fun connect() = Try {
        comms.connect()
    }.toEither { it.localizedMessage }.swap().toOption()

    override fun disconnect() = comms.disconnect()

    override fun isResourceInRange(
        resourceId: ResourceId,
        timeout: () -> Unit,
        success: (Boolean) -> Unit
    ) {
        val buffer = writeByteBuffers[isResourceInRangePacket.idOfCommand - startPacketId]
        val newCount = packetCounter.incrementAndGet()
        buffer.putInt(newCount)

        packetCallbacks[newCount] = timeout to { byteArray ->
            // The first 4 bytes are packet count
            // Next byte is a boolean for whether the resource was in range
            success(byteArray[4] == 0.toByte())
        }

        comms.writeBytes(
            isResourceInRangePacket.idOfCommand,
            buffer.array() + resourceId.validatedBytes()
        )

        isResourceInRangePacket.oneShotMode()
    }

    override fun provisionResource(
        resourceId: ResourceId,
        timeout: () -> Unit,
        success: (Boolean) -> Unit
    ) {
        TODO("not implemented")
    }

    override fun readProtocolVersion(timeout: () -> Unit, success: (String) -> Unit) {
        TODO("not implemented")
    }

    override fun analogRead(
        resourceId: ResourceId,
        timeout: () -> Unit,
        success: (Double) -> Unit
    ) {
        TODO("not implemented")
    }

    override fun analogWrite(
        resourceId: ResourceId,
        value: Long,
        timeout: () -> Unit,
        success: () -> Unit
    ) {
        TODO("not implemented")
    }

    override fun buttonRead(
        resourceId: ResourceId,
        timeout: () -> Unit,
        success: (Boolean) -> Unit
    ) {
        TODO("not implemented")
    }

    override fun digitalRead(
        resourceId: ResourceId,
        timeout: () -> Unit,
        success: (DigitalState) -> Unit
    ) {
        TODO("not implemented")
    }

    override fun digitalWrite(
        resourceId: ResourceId,
        value: DigitalState,
        timeout: () -> Unit,
        success: () -> Unit
    ) {
        TODO("not implemented")
    }

    override fun encoderRead(resourceId: ResourceId, timeout: () -> Unit, success: (Long) -> Unit) {
        TODO("not implemented")
    }

    override fun toneWrite(
        resourceId: ResourceId,
        frequency: Long,
        timeout: () -> Unit,
        success: () -> Unit
    ) {
        TODO("not implemented")
    }

    override fun toneWrite(
        resourceId: ResourceId,
        frequency: Long,
        duration: Long,
        timeout: () -> Unit,
        success: () -> Unit
    ) {
        TODO("not implemented")
    }

    override fun serialWrite(
        resourceId: ResourceId,
        message: String,
        timeout: () -> Unit,
        success: () -> Unit
    ) {
        TODO("not implemented")
    }

    override fun serialRead(
        resourceId: ResourceId,
        timeout: () -> Unit,
        success: (String) -> Unit
    ) {
        TODO("not implemented")
    }

    override fun servoWrite(
        resourceId: ResourceId,
        angle: Double,
        timeout: () -> Unit,
        success: () -> Unit
    ) {
        TODO("not implemented")
    }

    override fun servoRead(resourceId: ResourceId, timeout: () -> Unit, success: (Double) -> Unit) {
        TODO("not implemented")
    }

    override fun ultrasonicRead(
        resourceId: ResourceId,
        timeout: () -> Unit,
        success: (Long) -> Unit
    ) {
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
    fun getHighestPacketId() = startPacketId + 14

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
