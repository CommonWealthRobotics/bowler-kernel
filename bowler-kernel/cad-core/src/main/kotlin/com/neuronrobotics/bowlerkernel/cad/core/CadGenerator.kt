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
package com.neuronrobotics.bowlerkernel.cad.core

import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import com.neuronrobotics.bowlerkernel.kinematics.base.KinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.limb.LimbId
import eu.mihosoft.vrl.v3d.CSG

/**
 * A CAD generator which can generate the CSGs for a [KinematicBase].
 */
interface CadGenerator {

    /**
     * Generates the CSG for the body (the part which the limbs attach to).
     *
     * @param base The [KinematicBase].
     * @return The body CSG.
     */
    fun generateBody(base: KinematicBase): CSG

    /**
     * Generates the CSGs for the limbs.
     *
     * @param base The [KinematicBase].
     * @return The CSGs for the limbs.
     */
    fun generateLimbs(base: KinematicBase): ImmutableSetMultimap<LimbId, ImmutableSet<CSG>>
}
