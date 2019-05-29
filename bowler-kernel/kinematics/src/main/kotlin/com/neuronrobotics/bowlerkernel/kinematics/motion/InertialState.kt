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
package com.neuronrobotics.bowlerkernel.kinematics.motion

import Jama.Matrix

/**
 * The 15-dimensional state of a moving body.
 */
data class InertialState(
    val xPosition: Double = 0.0,
    val yPosition: Double = 0.0,
    val zPosition: Double = 0.0,
    val roll: Double = 0.0,
    val pitch: Double = 0.0,
    val yaw: Double = 0.0,
    val xVelocity: Double = 0.0,
    val yVelocity: Double = 0.0,
    val zVelocity: Double = 0.0,
    val rollRate: Double = 0.0,
    val pitchRate: Double = 0.0,
    val yawRate: Double = 0.0,
    val xAcceleration: Double = 0.0,
    val yAcceleration: Double = 0.0,
    val zAcceleration: Double = 0.0
) {

    constructor(
        xPosition: Number = 0,
        yPosition: Number = 0,
        zPosition: Number = 0,
        roll: Number = 0,
        pitch: Number = 0,
        yaw: Number = 0,
        xVelocity: Number = 0,
        yVelocity: Number = 0,
        zVelocity: Number = 0,
        rollRate: Number = 0,
        pitchRate: Number = 0,
        yawRate: Number = 0,
        xAcceleration: Number = 0,
        yAcceleration: Number = 0,
        zAcceleration: Number = 0
    ) : this(
        xPosition = xPosition.toDouble(),
        yPosition = yPosition.toDouble(),
        zPosition = zPosition.toDouble(),
        roll = roll.toDouble(),
        pitch = pitch.toDouble(),
        yaw = yaw.toDouble(),
        xVelocity = xVelocity.toDouble(),
        yVelocity = yVelocity.toDouble(),
        zVelocity = zVelocity.toDouble(),
        rollRate = rollRate.toDouble(),
        pitchRate = pitchRate.toDouble(),
        yawRate = yawRate.toDouble(),
        xAcceleration = xAcceleration.toDouble(),
        yAcceleration = yAcceleration.toDouble(),
        zAcceleration = zAcceleration.toDouble()
    )

    /**
     * The matrix representation of this state.
     */
    val state by lazy {
        Matrix(1, 15).apply {
            this[0, 0] = xPosition
            this[0, 1] = yPosition
            this[0, 2] = zPosition
            this[0, 3] = roll
            this[0, 4] = pitch
            this[0, 5] = yaw
            this[0, 6] = xVelocity
            this[0, 7] = yVelocity
            this[0, 8] = zVelocity
            this[0, 9] = rollRate
            this[0, 10] = pitchRate
            this[0, 11] = yawRate
            this[0, 12] = xAcceleration
            this[0, 13] = yAcceleration
            this[0, 14] = zAcceleration
        }
    }
}
