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
package com.neuronrobotics.bowlerkernel.vitamins

/**
 * A generic stepper motor.
 *
 * Good things to put in [Vitamin.specs]:
 *  - Mounting holes (Vitamin for the screws and hole positions)
 */
interface StepperMotor : Vitamin {

    /**
     * The shaft diameter.
     */
    val shaftDiameter: Double

    /**
     * The NEMA size.
     */
    val nemaSize: Int

    /**
     * The nominal voltage.
     */
    val voltage: Double

    /**
     * The holding torque.
     */
    val holdingTorque: Double

    /**
     * The rated current.
     */
    val current: Double

    /**
     * The degrees per step.
     */
    val stepAngle: Double
}
