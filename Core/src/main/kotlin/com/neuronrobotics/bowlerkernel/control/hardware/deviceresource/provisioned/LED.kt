/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned

import com.neuronrobotics.bowlerkernel.control.hardware.device.Device
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId

class LED(
    override val device: Device,
    override val resourceId: ResourceId
) : DigitalOut {

    override fun write(state: DigitalState) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
