package com.neuronrobotics.bowlerkernel.control.hardware.device

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.control.hardware.protocol.BowlerRPCProtocol
import com.neuronrobotics.bowlerkernel.control.hardware.registry.RegisterError

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
