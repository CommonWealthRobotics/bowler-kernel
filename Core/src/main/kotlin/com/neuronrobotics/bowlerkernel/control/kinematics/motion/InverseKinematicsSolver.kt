/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.motion

import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhParam

/**
 * A solver that implements inverse kinematics.
 */
interface InverseKinematicsSolver {

    /**
     * Solve the [chain] to produce new joint angles.
     *
     * @param chain The DH chain to solve for.
     * @param currentJointAngles The current joint angles.
     * @param targetFrameTransform The target task space frame transformation for the chain.
     * @return New joint angles to reach the [targetFrameTransform].
     */
    fun solveChain(
        chain: ImmutableList<DhParam>,
        currentJointAngles: ImmutableList<Double>,
        targetFrameTransform: FrameTransformation
    ): ImmutableList<Double>
}
