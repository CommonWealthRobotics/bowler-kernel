/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.kinematics.motion

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link

/**
 * A solver that implements forward kinematics.
 */
interface ForwardKinematicsSolver {

    /**
     * Solve the system to produce its current task space frame transformation.
     *
     * @param currentJointAngles The current joint angles.
     * @return The task space frame transformation of the chain given the [currentJointAngles].
     */
    fun solveChain(currentJointAngles: ImmutableList<Double>): FrameTransformation

    interface Factory {

        /**
         * Creates a [ForwardKinematicsSolver].
         *
         * @param links The links to solve for.
         */
        fun create(links: ImmutableList<Link>): ForwardKinematicsSolver
    }
}
