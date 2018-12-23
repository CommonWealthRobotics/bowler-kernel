/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.motion

import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhParam

/**
 * A motion planner which operates on a limb.
 */
interface LimbMotionPlanner {

    /**
     * Generates a plan to reach a task space transform.
     *
     * @param chain The DH chain to control.
     * @param currentTaskSpaceTransform The current task space transform.
     * @param targetTaskSpaceTransform The target task space transform.
     * @return A list of joint angles.
     */
    fun generatePlanForTaskSpaceTransform(
        chain: ImmutableList<DhParam>,
        currentTaskSpaceTransform: FrameTransformation,
        targetTaskSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    ): ImmutableList<ImmutableList<Double>>
}
