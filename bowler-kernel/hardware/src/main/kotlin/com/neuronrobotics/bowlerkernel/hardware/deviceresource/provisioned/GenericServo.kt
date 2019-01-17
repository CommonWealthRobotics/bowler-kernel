/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned

import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId

class GenericServo
internal constructor(
    override val device: Device,
    override val resourceId: ResourceId
) : Servo {

    override fun write(angle: Double) {
        TODO("not implemented")
    }

    override fun read(): Double {
        TODO("not implemented")
    }
}
