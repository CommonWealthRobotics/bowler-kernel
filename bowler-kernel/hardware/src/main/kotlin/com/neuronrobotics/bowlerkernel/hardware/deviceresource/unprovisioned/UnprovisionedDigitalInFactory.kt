/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.AttachmentPoint
import com.neuronrobotics.bowlerkernel.hardware.registry.RegisterError

interface UnprovisionedDigitalInFactory {

    /**
     * Makes an [UnprovisionedDigitalIn] attached to a device.
     *
     * @param attachmentPoint The attachment point.
     * @return An [UnprovisionedDigitalIn] on success, a [RegisterError] on failure.
     */
    fun makeUnprovisionedDigitalIn(
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedDigitalIn>

    interface Factory {

        /**
         * Factory for assisted injection.
         *
         * @param device The device this digital in is attached to.
         */
        fun create(device: BowlerDevice): UnprovisionedDigitalInFactory
    }
}
