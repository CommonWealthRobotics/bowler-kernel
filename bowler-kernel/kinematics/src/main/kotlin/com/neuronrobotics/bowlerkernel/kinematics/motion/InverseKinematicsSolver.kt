/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.kinematics.motion

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link

/**
 * A solver that implements inverse kinematics.
 */
interface InverseKinematicsSolver {

    /**
     * Solve the system to produce new joint angles.
     *
     * @param currentJointAngles The current joint angles.
     * @param targetFrameTransform The target task space frame transformation for the chain.
     * @return New joint angles to reach the [targetFrameTransform].
     */
    fun solveChain(
        currentJointAngles: ImmutableList<Double>,
        targetFrameTransform: FrameTransformation
    ): ImmutableList<Double>

    interface Factory {

        /**
         * Creates an [InverseKinematicsSolver].
         *
         * @param links The links to solve for.
         */
        fun create(links: ImmutableList<Link>): InverseKinematicsSolver
    }
}