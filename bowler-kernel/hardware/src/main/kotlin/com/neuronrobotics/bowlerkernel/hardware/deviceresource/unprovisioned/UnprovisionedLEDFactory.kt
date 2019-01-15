/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.registry.RegisterError

interface UnprovisionedLEDFactory {

    /**
     * Makes an [UnprovisionedLED] attached to a device.
     *
     * @param resourceId The resource id.
     * @return An [UnprovisionedLED] on success, a [RegisterError] on failure.
     */
    fun makeUnprovisionedLED(resourceId: ResourceId): Either<RegisterError, UnprovisionedLED>

    interface Factory {

        /**
         * Factory for assisted injection.
         *
         * @param device The device this LED is attached to.
         */
        fun create(device: BowlerDevice): UnprovisionedLEDFactory
    }
}
