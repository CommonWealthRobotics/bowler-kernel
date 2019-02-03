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
import com.neuronrobotics.bowlerkernel.hardware.protocol.AsyncBowlerRPCProtocol
import com.neuronrobotics.bowlerkernel.hardware.registry.RegisterError

interface BowlerDeviceFactory {

    /**
     * Makes a Bowler device (which runs the Bowler RPC protocol) with the specified id.
     *
     * @param deviceId The serial port the device is on.
     * @return A [BowlerDevice] on success, a [RegisterError] on failure.
     */
    fun makeBowlerDevice(
        deviceId: DeviceId,
        bowlerRPCProtocol: AsyncBowlerRPCProtocol
    ): Either<RegisterError, BowlerDevice>
}
