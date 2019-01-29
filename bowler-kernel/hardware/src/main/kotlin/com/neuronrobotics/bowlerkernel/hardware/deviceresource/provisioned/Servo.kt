/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
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
