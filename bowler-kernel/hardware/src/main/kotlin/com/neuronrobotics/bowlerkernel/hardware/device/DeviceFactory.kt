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
package com.neuronrobotics.bowlerkernel.hardware.device

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.protocol.BowlerRPCProtocol
import com.neuronrobotics.bowlerkernel.hardware.registry.HardwareRegistry
import com.neuronrobotics.bowlerkernel.hardware.registry.error.RegisterError

/**
 * A facade for making any type of device.
 */
class DeviceFactory(
    private val registry: HardwareRegistry,
    private val resourceIdValidator: ResourceIdValidator
) : BowlerDeviceFactory {

    override fun makeBowlerDevice(
        deviceId: DeviceId,
        bowlerRPCProtocol: BowlerRPCProtocol
    ): Either<RegisterError, BowlerDevice> =
        registry.registerDevice(deviceId) {
            BowlerDevice(it, bowlerRPCProtocol, resourceIdValidator)
        }
}
