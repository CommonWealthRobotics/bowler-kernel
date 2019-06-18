/*
 * This file is part of kinematics-chef.
 *
 * kinematics-chef is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kinematics-chef is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with kinematics-chef.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.kinematicschef.classifier

import arrow.core.Either
import arrow.core.Option
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation

interface WristIdentifier {

    /**
     * Computes whether the [chain] is a spherical wrist which is solvable with Euler angles.
     *
     * @param chain The chain to analyze.
     * @return True if the [chain] forms a spherical wrist, false otherwise.
     */
    fun isSphericalWrist(chain: ImmutableList<DhParam>): Option<String>

    /**
     * Computes whether the [chain] is a spherical wrist which is solvable with Euler angles.
     * Attempts to fix the DH parameters if they do not form a spherical wrist.
     *
     * @param chain The chain to analyze.
     * @param priorChain The part of the chain that lies before [chain].
     * @param inverseTipTransform The inverse of the tip frame transformation.
     * @return Left if the [chain] does not form a spherical wrist, right if the [chain] does
     * form a spherical wrist. If right, the DH parameters could be different than the input
     * parameters (they have been fixed to form a traditionally specified spherical wrist).
     */
    fun isSphericalWrist(
        chain: ImmutableList<DhParam>,
        priorChain: ImmutableList<DhParam>,
        inverseTipTransform: FrameTransformation
    ): Either<String, ImmutableList<DhParam>>
}
