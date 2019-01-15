/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.device

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.protocol.BowlerRPCProtocol
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
        bowlerRPCProtocol: BowlerRPCProtocol
    ): Either<RegisterError, BowlerDevice>
}
