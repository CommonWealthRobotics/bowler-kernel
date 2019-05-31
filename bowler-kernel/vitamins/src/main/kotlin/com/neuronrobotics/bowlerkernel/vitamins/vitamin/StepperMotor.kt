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

import org.octogonapus.ktunits.quantities.Angle
import org.octogonapus.ktunits.quantities.ElectricCurrent
import org.octogonapus.ktunits.quantities.ElectricPotential
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.Torque

/**
 * A generic stepper motor.
 *
 * For CAD, x=0, y=0 should be in the center of the output shaft. z=0 should be on the flat face
 * of the stepper motor.
 *
 * Good things to put in [Vitamin.specs]:
 *  - Mounting holes (Vitamin for the screws and hole positions)
 */
interface StepperMotor : Vitamin {

    /**
     * The overall width (x-axis), not including keep-away zones for CAD.
     */
    val width: Length

    /**
     * The overall height (y-axis), not including keep-away zones for CAD.
     */
    val height: Length

    /**
     * The NEMA size.
     */
    val nemaSize: Int

    /**
     * The nominal voltage.
     */
    val voltage: ElectricPotential

    /**
     * The holding torque.
     */
    val holdingTorque: Torque

    /**
     * The rated current.
     */
    val current: ElectricCurrent

    /**
     * The degrees per step.
     */
    val stepAngle: Angle

    /**
     * The shaft type.
     */
    val shaft: DefaultShaft
}
