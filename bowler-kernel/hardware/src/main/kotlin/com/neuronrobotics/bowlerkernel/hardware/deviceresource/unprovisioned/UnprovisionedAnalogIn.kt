/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.ProvisionError
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.GenericAnalogIn
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId

class UnprovisionedAnalogIn
internal constructor(
    override val device: BowlerDevice,
    override val resourceId: ResourceId
) : UnprovisionedDeviceResource {

    override fun provision(): Either<ProvisionError, GenericAnalogIn> {
        TODO("not implemented")
    }
}
