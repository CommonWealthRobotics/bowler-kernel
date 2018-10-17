package com.neuronrobotics.bowlerkernel.control.hardware.device

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.control.hardware.registry.RegisterError

interface BowlerDeviceFactory {

    fun makeBowlerDevice(deviceId: DeviceId): Either<RegisterError, BowlerDevice>
}
