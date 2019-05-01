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

import com.neuronrobotics.bowlerkernel.util.Limits

/**
 * A generic DC motor.
 *
 * Good things to put in [Vitamin.specs]:
 *  - Peak power RPM
 *  - Peak efficiency RPM
 *  - Mounting holes (Vitamin for the screws and a bolt circle diameter)
 */
interface DCMotor : Vitamin {

    /**
     * The operating voltage limits.
     */
    val voltage: Limits

    /**
     * The output shaft diameter.
     */
    val outputShaftDiameter: Double

    /**
     * The free speed.
     */
    val freeSpeed: Double

    /**
     * The free current.
     */
    val freeCurrent: Double

    /**
     * The stall torque.
     */
    val stallTorque: Double

    /**
     * The stall current.
     */
    val stallCurrent: Double

    /**
     * The maximum power output.
     */
    val power: Double
}
