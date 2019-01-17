/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned

import arrow.core.Either
import arrow.core.right
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.ProvisionError
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.GenericServo
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId

class UnprovisionedServo
internal constructor(
    override val device: BowlerDevice,
    override val resourceId: ResourceId
) : UnprovisionedDeviceResource {

    override fun provision(): Either<ProvisionError, GenericServo> {
        // TODO: Implement this
        return GenericServo(device, resourceId).right()
    }
}
