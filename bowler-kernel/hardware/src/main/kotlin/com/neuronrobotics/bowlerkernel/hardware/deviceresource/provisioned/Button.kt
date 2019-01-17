/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned

interface Button : ProvisionedDeviceResource {

    /**
     * Reads if the button is currently pressed.
     *
     * @return Whether the button is being pressed.
     */
    fun isPressed(): Boolean
}
