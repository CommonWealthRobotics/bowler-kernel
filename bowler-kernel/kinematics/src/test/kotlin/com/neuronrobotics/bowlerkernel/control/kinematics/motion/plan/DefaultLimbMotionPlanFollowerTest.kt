/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.motion.plan

import com.neuronrobotics.bowlerkernel.control.createMotionConstraints
import com.neuronrobotics.bowlerkernel.control.kinematics.MockJointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.DefaultLimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlan
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanStep
import com.neuronrobotics.bowlerkernel.util.emptyImmutableList
import com.neuronrobotics.bowlerkernel.util.immutableListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.abs

internal class DefaultLimbMotionPlanFollowerTest {

    private val controller =
        MockJointAngleController()

    private val follower = DefaultLimbMotionPlanFollower(immutableListOf(controller))

    @Test
    @Tag("performance")
    fun `test target timing`() {
        val timestep = 100
        val plan = LimbMotionPlan(
            immutableListOf(
                LimbMotionPlanStep(immutableListOf(0.0), createMotionConstraints(timestep)),
                LimbMotionPlanStep(immutableListOf(1.0), createMotionConstraints(timestep))
            )
        )

        fun timeOnce(): Long {
            follower.followPlan(plan)
            runBlocking { delay(timestep * 4L) }
            val time = controller.times[1] - controller.times[0]
            controller.times.clear()
            controller.targets.clear()
            return time
        }

        val averageTiming = (1..10).toList().map {
            timeOnce()
        }.average()

        assertTrue(abs(averageTiming - timestep) < timestep * 0.2) {
            "The average timing was too large: $averageTiming"
        }
    }

    @Test
    fun `test plan length validation with too few target joint angles`() {
        val plan = LimbMotionPlan(
            immutableListOf(
                LimbMotionPlanStep(emptyImmutableList(), createMotionConstraints(0))
            )
        )

        assertThrows<IllegalArgumentException> {
            follower.followPlan(plan)
        }
    }

    @Test
    fun `test plan length validation with too many target joint angles`() {
        val plan = LimbMotionPlan(
            immutableListOf(
                LimbMotionPlanStep(immutableListOf(0.0, 0.0), createMotionConstraints(0))
            )
        )

        assertThrows<IllegalArgumentException> {
            follower.followPlan(plan)
        }
    }
}
