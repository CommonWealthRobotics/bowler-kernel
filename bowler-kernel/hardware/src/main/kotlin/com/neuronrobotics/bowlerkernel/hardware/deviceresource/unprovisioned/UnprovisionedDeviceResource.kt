/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.DeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.ProvisionError
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.ProvisionedDeviceResource

/**
 * A [DeviceResource] which has been registered but not provisioned.
 */
interface UnprovisionedDeviceResource : DeviceResource {

    /**
     * Provisions this [DeviceResource] by communicating with the parent device to set up any
     * hardware-local requirements. For example, this could initialize GPIO pins or register
     * interrupt handlers.
     *
     * @return A [ProvisionedDeviceResource] on success, a [ProvisionError] on failure.
     */
    fun provision(): Either<ProvisionError, ProvisionedDeviceResource>
}
