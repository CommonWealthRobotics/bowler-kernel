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
package com.neuronrobotics.bowlerkernel.util

/**
 * Contains the servo limits.
 *
 * @param maximum The absolute maximum servo angle in servo units.
 * @param minimum The absolute minimum servo angle in servo units.
 * @param zero The zero servo angle in servo units.
 * @param scale A scale that relates servo units to degrees in units (degrees / (servo units)).
 */
data class ServoLimits(
    val maximum: Double,
    val minimum: Double,
    val zero: Double,
    val scale: Double
) {
    constructor(
        maximum: Number,
        minimum: Number,
        zero: Number,
        scale: Number
    ) : this(maximum.toDouble(), minimum.toDouble(), zero.toDouble(), scale.toDouble())

    /**
     * Converts the maximum and minimum fields into a [JointLimits].
     */
    fun toJointLimits() = JointLimits(
        maximum = (maximum - zero) * scale,
        minimum = (minimum - zero) * scale
    )
}
