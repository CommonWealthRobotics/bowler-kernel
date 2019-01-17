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

interface UnprovisionedPiezoelectricSpeakerFactory {

    /**
     * Makes an [UnprovisionedPiezoelectricSpeaker] attached to a device.
     *
     * @param attachmentPoint The attachment point.
     * @return An [UnprovisionedPiezoelectricSpeaker] on success, a [RegisterError] on failure.
     */
    fun makeUnprovisionedPiezoelectricSpeaker(
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedPiezoelectricSpeaker>

    interface Factory {

        /**
         * Factory for assisted injection.
         *
         * @param device The device this piezoelectric speaker is attached to.
         */
        fun create(device: BowlerDevice): UnprovisionedPiezoelectricSpeakerFactory
    }
}
