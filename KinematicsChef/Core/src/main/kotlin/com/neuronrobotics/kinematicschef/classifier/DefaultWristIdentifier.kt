/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import arrow.core.Either
import arrow.core.Option
import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.util.asPointMatrix
import com.neuronrobotics.kinematicschef.util.getTranslation
import org.ejml.simple.SimpleMatrix

internal class DefaultWristIdentifier : WristIdentifier {

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
            fun centerLinkNoOffset() = chain[1].r == 0.0 && chain[1].d == 0.0

            if ((config1() || config2()) && centerLinkNoOffset()) {
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
                    val wristCenter = SphericalWrist(chain).centerHomed(priorChain).asPointMatrix()
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
}
