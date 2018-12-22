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
import org.ejml.simple.SimpleMatrix

interface WristIdentifier {

    /**
     * Computes whether the [chain] is a spherical wrist which is solvable with Euler angles.
     *
     * @param chain The chain to analyze.
     * @return True if the [chain] forms a spherical wrist, false otherwise.
     */
    fun isSphericalWrist(chain: ImmutableList<DhParam>): Option<ClassifierError>

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
        inverseTipTransform: SimpleMatrix
    ): Either<ClassifierError, ImmutableList<DhParam>>
}
