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
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup

data class IMUState(
    val roll: Double = 0.0,
    val pitch: Double = 0.0,
    val yaw: Double = 0.0,
    val rollRate: Double = 0.0,
    val pitchRate: Double = 0.0,
    val yawRate: Double = 0.0,
    val xAcceleration: Double = 0.0,
    val yAcceleration: Double = 0.0,
    val zAcceleration: Double = 0.0
)
