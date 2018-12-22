/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.motion

import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhParam

/**
 * A solver that implements forward kinematics.
 */
interface ForwardKinematicsSolver {

    /**
     * Solve the [chain] to produce its current task space frame transformation.
     *
     * @param chain The DH chain to solve for.
     * @param currentJointAngles The current joint angles.
     * @return The task space frame transformation of the chain given the [currentJointAngles].
     */
    fun solveChain(
        chain: ImmutableList<DhParam>,
        currentJointAngles: ImmutableList<Double>
    ): FrameTransformation
}
