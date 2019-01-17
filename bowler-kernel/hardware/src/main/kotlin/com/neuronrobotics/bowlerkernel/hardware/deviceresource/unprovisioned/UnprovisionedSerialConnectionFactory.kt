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

interface UnprovisionedSerialConnectionFactory {

    /**
     * Makes an [UnprovisionedSerialConnection] attached to a device.
     *
     * @param attachmentPoint The attachment point.
     * @return An [UnprovisionedSerialConnection] on success, a [RegisterError] on failure.
     */
    fun makeUnprovisionedSerialConnection(
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedSerialConnection>

    interface Factory {

        /**
         * Factory for assisted injection.
         *
         * @param device The device this serial connection is attached to.
         */
        fun create(device: BowlerDevice): UnprovisionedSerialConnectionFactory
    }
}
