/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.control.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.PinNumber
import com.neuronrobotics.bowlerkernel.control.hardware.registry.RegisterError

interface UnprovisionedLEDFactory {

    /**
     * Makes an LED attached to a device.
     *
     * @param resourceId The resource id of the LED.
     * @return An [UnprovisionedLED] on success, a [RegisterError] on failure.
     */
    fun makeUnprovisionedLED(pinNumber: PinNumber): Either<RegisterError, UnprovisionedLED>

    interface Factory {

        /**
         * Factory for assisted injection.
         *
         * @param device The device this LED is attached to.
         */
        fun create(device: BowlerDevice): UnprovisionedLEDFactory
    }
}
