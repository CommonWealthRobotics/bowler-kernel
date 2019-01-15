/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned

import com.neuronrobotics.bowlerkernel.control.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId

class LED
internal constructor(
    override val device: BowlerDevice,
    override val resourceId: ResourceId
) : DigitalOut {

    override fun write(state: DigitalState) {
        TODO("not implemented")
    }

    override fun toString() = """`$resourceId` attached to $device"""
}
