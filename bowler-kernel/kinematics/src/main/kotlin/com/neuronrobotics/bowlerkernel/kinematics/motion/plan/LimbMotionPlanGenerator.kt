/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
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
