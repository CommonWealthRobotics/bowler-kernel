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
package com.neuronrobotics.bowlerkernel.vitamins.vitamin

import org.octogonapus.ktunits.quantities.AngularVelocity
import org.octogonapus.ktunits.quantities.ElectricPotential
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.Torque

/**
 * A hobby servo motor.
 *
 * For CAD, x=0, y=0 should be in the center of the output shaft. z=0 should be on the top of
 * the flange face of the servo.
 *
 * Good things to put in [Vitamin.specs]:
 *  - Any supported feedback
 */
interface Servo : Vitamin {

    /**
     * The width of the body (between the two large rectangular sides).
     */
    val width: Length

    /**
     * The length of the body (between the face the shaft is on and the opposite face).
     */
    val length: Length

    /**
     * The height of the body (between the face the wire attaches on and the opposite face).
     */
    val height: Length

    /**
     * The distance between the center of the shaft and the top of the body where the wire attaches
     * (not to the top of the flange).
     */
    val shaftCenterToTopOfBody: Length

    /**
     * The flange height from the top face to the bottom face (i.e., including the
     * body height).
     */
    val flangeHeight: Length

    /**
     * The flange width from the left face to the right face (i.e., including the body width).
     */
    val flangeWidth: Length

    /**
     * The flange thickness (between the two large flat faces of the flange).
     */
    val flangeThickness: Length

    /**
     * The height between the bolt hole centers (the longer dimension).
     */
    val boltHeightSeparation: Length

    /**
     * The width between the bolt hole centers (the smaller dimension).
     */
    val boltWidthSeparation: Length

    /**
     * The operating voltage.
     */
    val voltage: ElectricPotential

    /**
     * The stall torque.
     */
    val stallTorque: Torque

    /**
     * The operating speed.
     */
    val speed: AngularVelocity

    /**
     * The shaft type.
     */
    val shaft: DefaultShaft
}
