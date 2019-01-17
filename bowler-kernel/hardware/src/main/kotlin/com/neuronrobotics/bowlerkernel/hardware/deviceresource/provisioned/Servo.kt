/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned

interface Servo : ProvisionedDeviceResource {

    /**
     * Sets the angle of the servo. On a continuous rotation servo, this will set the speed
     * instead of the angle.
     *
     * @param angle The angle to write.
     */
    fun write(angle: Double)

    /**
     * Reads the current angle of the servo. In some cases, this is just the last value passed to
     * [write].
     *
     * @return The current angle of the servo.
     */
    fun read(): Double
}
