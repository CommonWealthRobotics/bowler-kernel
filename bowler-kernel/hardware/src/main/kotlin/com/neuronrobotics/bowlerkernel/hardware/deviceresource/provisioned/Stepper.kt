/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned

interface Stepper : ProvisionedDeviceResource {

    /**
     * Turns the motor a number of [steps] at a rate of [speed] rpm.
     *
     * @param steps The number of steps to turn.
     * @param speed The speed to turn at.
     */
    fun step(steps: Int, speed: Int)
}
