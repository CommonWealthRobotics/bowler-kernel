/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource

import com.neuronrobotics.bowlerkernel.control.hardware.device.Device
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId

/**
 * A resource attached (physically) to a [Device].
 */
interface DeviceResource {

    /**
     * The [Device] this resource is attached to.
     */
    val device: Device

    /**
     * The id of this resource. Typically a pin number.
     */
    val resourceId: ResourceId
}
