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

import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultBolt
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultShaft
import org.octogonapus.ktunits.quantities.Angle
import org.octogonapus.ktunits.quantities.AngularVelocity
import org.octogonapus.ktunits.quantities.ElectricCurrent
import org.octogonapus.ktunits.quantities.ElectricPotential
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.Power
import org.octogonapus.ktunits.quantities.Torque

/**
 * A generic DC motor.
 *
 * For CAD, x=0, y=0 should be in the center of the output shaft. z=0 should be on the flat face
 * of the motor.
 *
 * Good things to put in [Vitamin.specs]:
 *  - Peak power RPM
 *  - Peak efficiency RPM
 *  - Mounting holes (Vitamin for the screws and a bolt circle diameter)
 */
@SuppressWarnings("ComplexInterface")
interface DCMotor : Vitamin {

    /**
     * The diameter of the case.
     */
    val diameter: Length

    /**
     * The overall height (z-axis), not including keep-away zones for CAD.
     */
    val height: Length

    /**
     * The shaft type.
     */
    val shaft: DefaultShaft

    /**
     * The diameter of the shaft support.
     */
    val shaftSupportDiameter: Length

    /**
     * The height of the shaft support.
     */
    val shaftSupportHeight: Length

    /**
     * The bolt type.
     */
    val bolt: DefaultBolt

    /**
     * The diameter of the bolt circle.
     */
    val boltCircleDiameter: Length

    /**
     * The angle between bolt holes.
     */
    val boltCircleAngleIncrement: Angle

    /**
     * The angle from vertical to the first bolt hole.
     */
    val boltCircleAngleOffset: Angle

    /**
     * The operating voltage.
     */
    val voltage: ElectricPotential

    /**
     * The free speed.
     */
    val freeSpeed: AngularVelocity

    /**
     * The free current.
     */
    val freeCurrent: ElectricCurrent

    /**
     * The stall torque.
     */
    val stallTorque: Torque

    /**
     * The stall current.
     */
    val stallCurrent: ElectricCurrent

    /**
     * The maximum power output.
     */
    val power: Power
}
