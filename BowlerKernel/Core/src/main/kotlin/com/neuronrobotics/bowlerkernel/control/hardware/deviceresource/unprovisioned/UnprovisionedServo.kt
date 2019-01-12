/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.control.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.ProvisionError
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned.Servo
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId

class UnprovisionedServo
internal constructor(
    override val device: BowlerDevice,
    override val resourceId: ResourceId
) : UnprovisionedDeviceResource {

    override fun provision(): Either<ProvisionError, Servo> =
        device.provisionResource { Servo(device, resourceId) }
}
