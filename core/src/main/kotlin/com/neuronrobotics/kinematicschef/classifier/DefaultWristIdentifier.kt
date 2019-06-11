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
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.util.toTranslation
import com.neuronrobotics.kinematicschef.util.getTranslation
import org.ejml.simple.SimpleMatrix

class DefaultWristIdentifier
internal constructor() : WristIdentifier {

    /**
     * Computes whether the [chain] is a spherical wrist which is solvable with Euler angles.
     *
     * @param chain The chain to analyze.
     * @return True if the [chain] forms a spherical wrist, false otherwise.
     */
    override fun isSphericalWrist(chain: ImmutableList<DhParam>): Option<ClassifierError> {
        return if (chain.size == 3) {
            fun config1() = chain[0].alpha == -90.0 && chain[1].alpha == 90.0
            fun config2() = chain[0].alpha == 90.0 && chain[1].alpha == -90.0
            fun config3() = chain[0].alpha == 0.0 && chain[1].alpha == 90.0 &&
                chain[2].alpha == -90.0

            fun config4() = chain[0].alpha == 0.0 && chain[1].alpha == -90.0 &&
                chain[2].alpha == 90.0

            fun centerLinkNoOffset() = chain[1].r == 0.0 && chain[1].d == 0.0

            if ((config1() || config2() || config3() || config4()) && centerLinkNoOffset()) {
                Option.empty()
            } else {
                Option.just(ClassifierError("Not spherical."))
            }
        } else {
            Option.just(
                ClassifierError(
                    "A chain of ${chain.size} links cannot form a spherical wrist"
                )
            )
        }
    }

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
    override fun isSphericalWrist(
        chain: ImmutableList<DhParam>,
        priorChain: ImmutableList<DhParam>,
        inverseTipTransform: SimpleMatrix
    ): Either<ClassifierError, ImmutableList<DhParam>> {
        return if (chain.size == 3) {
            isSphericalWrist(chain).fold(
                { Either.right(chain) },
                {
                    val wristCenter = SphericalWrist(chain).centerHomed(priorChain).toTranslation()
                    val position = wristCenter.mult(inverseTipTransform).getTranslation()
                    position.print()
                    if (position[1, 0] == 0.0 && position[2, 0] == 0.0) {
                        // The center of the wrist lies on the x-axis so it is spherical
                        TODO("Not implemented")
                    } else {
                        // The center of the wrist does not lie on the x-axis so it cannot be spherical
                        Either.left(ClassifierError("Not spherical."))
                    }
                }
            )
        } else {
            Either.left(
                ClassifierError("A chain of ${chain.size} links cannot form a spherical wrist")
            )
        }
    }

    companion object {
        fun create() = DefaultWristIdentifier()
    }
}
