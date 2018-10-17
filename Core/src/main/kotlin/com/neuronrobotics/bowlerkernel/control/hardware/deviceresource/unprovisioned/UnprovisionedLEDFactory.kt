package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.control.hardware.device.Device
import com.neuronrobotics.bowlerkernel.control.hardware.registry.RegisterError

interface UnprovisionedLEDFactory {

    fun makeUnprovisionedLED(pinNumber: Int): Either<RegisterError, UnprovisionedLED>

    interface Factory {

        fun create(device: Device): UnprovisionedLEDFactory
    }
}
