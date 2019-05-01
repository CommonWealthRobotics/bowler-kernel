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
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.JointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb

/**
 * A motion plan follower which operates on a limb.
 */
interface LimbMotionPlanFollower {

    /**
     * Follows a [LimbMotionPlan].
     *
     * @param limb The limb to follow the plan with.
     * @param plan The [LimbMotionPlan] to follow.
     */
    fun followPlan(limb: Limb, plan: LimbMotionPlan)

    interface Factory {

        /**
         * Creates a [LimbMotionPlanFollower].
         *
         * @param jointAngleControllers The joint angle controllers.
         */
        fun create(
            jointAngleControllers: ImmutableList<JointAngleController>
        ): LimbMotionPlanFollower
    }
}
