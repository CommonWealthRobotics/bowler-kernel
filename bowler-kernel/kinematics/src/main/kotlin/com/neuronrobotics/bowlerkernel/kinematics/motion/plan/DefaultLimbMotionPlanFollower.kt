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
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * A [LimbMotionPlanFollower] which schedules each plan step using an
 * [Executors.newScheduledThreadPool] and sets the step's target joint angles to the
 * [jointAngleControllers].
 *
 * @param jointAngleControllers The controllers for the joints.
 */
class DefaultLimbMotionPlanFollower(
    private val jointAngleControllers: ImmutableList<JointAngleController>
) : LimbMotionPlanFollower {

    override fun followPlan(plan: LimbMotionPlan) {
        if (plan.steps.isEmpty()) {
            return
        }

        plan.steps.forEach {
            require(it.jointAngles.size == jointAngleControllers.size) {
                """
                |Must have an equal number of target joint angles and joint angle controllers.
                |Number of target joint angles in the first plan step: $it.jointAngles.size
                |Number of joint angle controllers: ${jointAngleControllers.size}
                """.trimMargin()
            }
        }

        val pool = Executors.newScheduledThreadPool(1)

        // Schedule each plan step
        var timestepSum = 0L
        plan.steps.map { step ->
            val scheduled = pool.schedule(
                {
                    step.jointAngles.forEachIndexed { index, targetJointAngle ->
                        jointAngleControllers[index].setTargetAngle(
                            targetJointAngle,
                            step.motionConstraints
                        )
                    }
                },
                timestepSum + step.motionConstraints.motionDuration.toLong(),
                TimeUnit.MILLISECONDS
            )
            timestepSum += step.motionConstraints.motionDuration.toLong()
            scheduled
        }
    }
}
