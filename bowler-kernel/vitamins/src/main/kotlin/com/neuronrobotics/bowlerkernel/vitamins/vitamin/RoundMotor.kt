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
import org.octogonapus.ktunits.quantities.AngularVelocity
import org.octogonapus.ktunits.quantities.ElectricCurrent
import org.octogonapus.ktunits.quantities.ElectricPotential
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.Power
import org.octogonapus.ktunits.quantities.Torque

/**
 * A round motor (dc motor with a gearbox attached).
 */
interface RoundMotor : Vitamin {

    /**
     * The diameter of the shaft support around the shaft, at the output of the gearbox.
     */
    val shaftSupportDiameter: Length

    /**
     * The height of the shaft support around the shaft, at the output of the gearbox.
     */
    val shaftSupportHeight: Length

    /**
     * The offset distance from the center of the gearbox to the center of the shaft.
     */
    val gearboxShaftOffset: Length

    /**
     * The diameter of the gearbox.
     */
    val gearboxDiameter: Length

    /**
     * The height of the gearbox.
     */
    val gearboxHeight: Length

    /**
     * The diameter of the motor.
     */
    val motorDiameter: Length

    /**
     * The height of the motor.
     */
    val motorHeight: Length

    /**
     * The diameter of the encoder assembly.
     */
    val encoderAssemblyDiameter: Length

    /**
     * The height of the encoder assembly.
     */
    val encoderAssemblyHeight: Length

    /**
     * The shaft type. The height of the shaft does not include the [shaftSupportHeight].
     */
    val shaft: DefaultShaft

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
