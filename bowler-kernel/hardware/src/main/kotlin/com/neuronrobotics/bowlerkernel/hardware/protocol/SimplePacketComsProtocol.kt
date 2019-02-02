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

/**
 * An implementation of [AsyncBowlerRPCProtocol] using SimplePacketComs. Uses a continuous range of
 * packet ids from [getLowestPacketId] through [getHighestPacketId]. Any numbers outside that
 * range are available for other packets.
 *
 * @param comms The comms implementation.
 * @param startPacketId The starting range of the packets this class creates.
 */
class SimplePacketComsProtocol(
    private val comms: AbstractSimpleComsDevice,
    private val startPacketId: Int = 1
) : AsyncBowlerRPCProtocol {

    private val isResourceInRangePacket = BytePacketType(startPacketId, 64)
    private val provisionResourcePacket = BytePacketType(startPacketId + 1, 64)
    private val readProtocolVersionPacket = BytePacketType(startPacketId + 2, 64)
    private val analogReadPacket = FloatPacketType(startPacketId + 3, 64)
    private val analogWritePacket = BytePacketType(startPacketId + 4, 64)
    private val buttonReadPacket = BytePacketType(startPacketId + 5, 64)
    private val digitalReadPacket = BytePacketType(startPacketId + 6, 64)
    private val digitalWritePacket = BytePacketType(startPacketId + 7, 64)
    private val encoderReadPacket = BytePacketType(startPacketId + 8, 64)
    private val toneWritePacket = FloatPacketType(startPacketId + 9, 64)
    private val serialWritePacket = BytePacketType(startPacketId + 10, 64)
    private val serialReadPacket = BytePacketType(startPacketId + 11, 64)
    private val servoWritePacket = FloatPacketType(startPacketId + 12, 64)
    private val servoReadPacket = FloatPacketType(startPacketId + 13, 64)
    private val ultrasonicReadPacket = FloatPacketType(startPacketId + 14, 64)

    override fun connect() = Try {
        comms.connect()
    }.toEither { it.localizedMessage }.swap().toOption()

    override fun disconnect() = comms.disconnect()

    override fun isResourceInRange(
        resourceId: ResourceId,
        timeout: () -> Unit,
        success: (Boolean) -> Unit
    ) {
        var eventCallback = {}
        eventCallback = {
            /**
             * Byte 0: 0 for true, 1 for false
             */
            success(comms.readBytes(isResourceInRangePacket.idOfCommand)[0] == 0.toByte())
            comms.removeEvent(isResourceInRangePacket.idOfCommand, eventCallback)
        }

        comms.addEvent(isResourceInRangePacket.idOfCommand, eventCallback)

        comms.addTimeout(isResourceInRangePacket.idOfCommand) {
            comms.removeEvent(isResourceInRangePacket.idOfCommand, eventCallback)
            timeout()
        }

        comms.writeBytes(isResourceInRangePacket.idOfCommand, ByteArray(0))
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
}
