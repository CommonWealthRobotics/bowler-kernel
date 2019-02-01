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

import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice
import edu.wpi.SimplePacketComs.BytePacketType

/**
 * An implementation of [BowlerRPCProtocol] using SimplePacketComs.
 *
 * @param comms The comms implementation.
 */
class SimplePacketComsProtocol(
    private val comms: AbstractSimpleComsDevice
) : BowlerRPCProtocol {

    /**
     * Byte 0: 0 for true, 1 for false
     */
    private val isResourceInRangePacket = BytePacketType(1, 64)
    private val rbePacketRangeStart = 1400
    private val rbePacketRangeEnd = 2025

    override fun isResourceInRange(
        resourceId: ResourceId,
        timeout: () -> Unit,
        success: (Boolean) -> Unit
    ) {
        var eventCallback = {}
        eventCallback = {
            // TODO: Replace this once
            // https://github.com/madhephaestus/SimplePacketComsJava/issues/1 is done
            if (comms.isTimedOut) {
                timeout()
            } else {
                success(comms.readBytes(isResourceInRangePacket.idOfCommand)[0] == 0.toByte())
            }

            // TODO: This line will break things until
            // https://github.com/madhephaestus/SimplePacketComsJava/issues/2 is done
            comms.removeEvent(isResourceInRangePacket.idOfCommand, eventCallback)
        }

        comms.addEvent(isResourceInRangePacket.idOfCommand, eventCallback)
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
}
