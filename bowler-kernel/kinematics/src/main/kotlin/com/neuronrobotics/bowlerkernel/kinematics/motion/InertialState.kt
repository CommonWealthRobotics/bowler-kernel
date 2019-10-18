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

/**
 * The 15-dimensional state of a moving body.
 */
data class InertialState(
    val xPosition: Double? = null,
    val yPosition: Double? = null,
    val zPosition: Double? = null,
    val roll: Double? = null,
    val pitch: Double? = null,
    val yaw: Double? = null,
    val xVelocity: Double? = null,
    val yVelocity: Double? = null,
    val zVelocity: Double? = null,
    val rollRate: Double? = null,
    val pitchRate: Double? = null,
    val yawRate: Double? = null,
    val xAcceleration: Double? = null,
    val yAcceleration: Double? = null,
    val zAcceleration: Double? = null
) {

    constructor(
        xPosition: Number? = null,
        yPosition: Number? = null,
        zPosition: Number? = null,
        roll: Number? = null,
        pitch: Number? = null,
        yaw: Number? = null,
        xVelocity: Number? = null,
        yVelocity: Number? = null,
        zVelocity: Number? = null,
        rollRate: Number? = null,
        pitchRate: Number? = null,
        yawRate: Number? = null,
        xAcceleration: Number? = null,
        yAcceleration: Number? = null,
        zAcceleration: Number? = null
    ) : this(
        xPosition = xPosition?.toDouble(),
        yPosition = yPosition?.toDouble(),
        zPosition = zPosition?.toDouble(),
        roll = roll?.toDouble(),
        pitch = pitch?.toDouble(),
        yaw = yaw?.toDouble(),
        xVelocity = xVelocity?.toDouble(),
        yVelocity = yVelocity?.toDouble(),
        zVelocity = zVelocity?.toDouble(),
        rollRate = rollRate?.toDouble(),
        pitchRate = pitchRate?.toDouble(),
        yawRate = yawRate?.toDouble(),
        xAcceleration = xAcceleration?.toDouble(),
        yAcceleration = yAcceleration?.toDouble(),
        zAcceleration = zAcceleration?.toDouble()
    )
}
