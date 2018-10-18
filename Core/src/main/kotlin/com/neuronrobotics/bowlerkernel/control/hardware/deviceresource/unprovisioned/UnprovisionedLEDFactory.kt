package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.control.hardware.device.Device
import com.neuronrobotics.bowlerkernel.control.hardware.registry.RegisterError

interface UnprovisionedLEDFactory {

    /**
     * Makes an LED attached to a device.
     *
     * @param pinNumber The pin number the LED is attached to.
     * @return An [UnprovisionedLED] on success, a [RegisterError] on failure.
     */
    fun makeUnprovisionedLED(pinNumber: Int): Either<RegisterError, UnprovisionedLED>

    interface Factory {

        /**
         * Factory for assisted injection.
         *
         * @param device The device this LED is attached to.
         */
        fun create(device: Device): UnprovisionedLEDFactory
    }
}
