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
     * The width of the body (between the face the wire attaches on and the opposite face).
     */
    val width: Length

    /**
     * The depth of the body (between the two large rectangular sides).
     */
    val depth: Length

    /**
     * The height of the body (between the face the shaft is on and the opposite face).
     */
    val height: Length

    /**
     * The distance between the center of the shaft and the top of the body where the wire attaches
     * (not to the top of the flange).
     */
    val shaftCenterToTopOfBody: Length

    /**
     * The flange width from the top face to the bottom face (i.e., including the
     * body width).
     */
    val flangeWidth: Length

    /**
     * The flange depth from the left face to the right face (i.e., including the body depth).
     */
    val flangeDepth: Length

    /**
     * The flange height (between the two large flat faces of the flange).
     */
    val flangeHeight: Length

    /**
     * The distance between the bottom face of the body and the bottom face of the flange.
     */
    val flangeHeightFromBottomOfBody: Length

    /**
     * The width between the bolt hole centers (the longer dimension).
     */
    val boltWidthSeparation: Length

    /**
     * The depth between the bolt hole centers (the smaller dimension).
     */
    val boltDepthSeparation: Length

    /**
     * The diameter of the bolt holes.
     */
    val boltHoleDiameter: Length

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
    val shaft: Shaft
}
