/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.motion.plan

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.control.closedloop.JointAngleController
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
        if (plan.steps.size != 0) {
            val targets = plan.steps.first().jointAngles.size
            require(targets == jointAngleControllers.size) {
                """
                |Must have an equal number of target joint angles and joint angle controllers.
                |Number of target joint angles in the first plan step: $targets
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
