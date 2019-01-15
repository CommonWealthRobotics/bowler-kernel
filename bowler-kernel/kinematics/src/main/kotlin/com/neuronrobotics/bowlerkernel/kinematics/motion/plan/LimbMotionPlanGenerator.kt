/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.kinematics.motion.plan

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints

/**
 * A motion plan generator which operates on a limb.
 */
interface LimbMotionPlanGenerator {

    /**
     * Generates a plan to reach a task space transform.
     *
     * @param currentTaskSpaceTransform The current task space transform.
     * @param targetTaskSpaceTransform The target task space transform.
     * @return A list of joint angles.
     */
    fun generatePlanForTaskSpaceTransform(
        currentTaskSpaceTransform: FrameTransformation,
        targetTaskSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    ): LimbMotionPlan

    interface Factory {

        /**
         * Creates a [LimbMotionPlanGenerator].
         *
         * @param links The links to control.
         */
        fun create(links: ImmutableList<Link>): LimbMotionPlanGenerator
    }
}
