/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid

/**
 * The id of a resource attached (physically) to a device.
 */
interface ResourceId {

    /**
     * The value of this device id as a string.
     */
    override fun toString(): String
}
