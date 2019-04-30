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

import com.google.common.collect.ImmutableMap
import com.neuronrobotics.bowlerkernel.gitfs.GitFile

data class DefaultStepperMotor(
    override val shaftDiameter: Double,
    override val nemaSize: Int,
    override val voltage: Double,
    override val holdingTorque: Double,
    override val current: Double,
    override val stepAngle: Double,
    override val width: Double,
    override val length: Double,
    override val height: Double,
    override val weight: Double,
    override val centerOfMass: CenterOfMass,
    override val specs: ImmutableMap<String, Any>,
    override val cadGenerator: GitFile
) : StepperMotor
