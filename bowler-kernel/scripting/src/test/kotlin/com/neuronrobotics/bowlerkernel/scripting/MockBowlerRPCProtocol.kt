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
package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.Option
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.protocol.AsyncBowlerRPCProtocol
import org.junit.jupiter.api.fail

/**
 * A mock implementation of the [AsyncBowlerRPCProtocol] which just checks for simple management of
 * connection state.
 */
class MockBowlerRPCProtocol : AsyncBowlerRPCProtocol {

    private var isConnected = false

    override fun connect(): Option<String> {
        isConnected = true
        return Option.empty()
    }

    override fun disconnect() {
        isConnected = false
    }

    override fun isResourceInRange(resourceId: ResourceId): Boolean {
        if (isConnected) {
            Thread.sleep(5)
            return true
        } else {
            fail { "The RPC is not connected!" }
        }
    }

    override fun provisionResource(resourceId: ResourceId): Boolean {
        if (isConnected) {
            Thread.sleep(5)
            return true
        } else {
            fail { "The RPC is not connected!" }
        }
    }

    override fun readProtocolVersion(): String {
        if (isConnected) {
            Thread.sleep(5)
            return ""
        } else {
            fail { "The RPC is not connected!" }
        }
    }

    override fun analogRead(resourceId: ResourceId): Double {
        TODO("not implemented")
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
}
