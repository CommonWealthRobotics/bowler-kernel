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
import com.neuronrobotics.bowlerkernel.kinematics.MockJointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.JointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.createMotionConstraints
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.toImmutableList
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@Timeout(value = 15, unit = TimeUnit.SECONDS)
internal class DefaultLimbMotionPlanFollowerTest {

    private val controller = MockJointAngleController()

    @Suppress("UNCHECKED_CAST")
    private val limb = mock<Limb> {
        on { jointAngleControllers } doReturn
            immutableListOf(controller) as ImmutableList<JointAngleController>
    }

    private val follower = DefaultLimbMotionPlanFollower()

    @Test
    @Tag("performance")
    fun `test target timing`() {
        val timestep = 100
        val plan = LimbMotionPlan(
            immutableListOf(
                LimbMotionPlanStep(
                    immutableListOf(0.0),
                    createMotionConstraints(timestep)
                ),
                LimbMotionPlanStep(
                    immutableListOf(1.0),
                    createMotionConstraints(timestep)
                )
            )
        )

        fun timeOnce(): Long {
            follower.followPlan(limb.jointAngleControllers, plan)
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
    fun `test angles are set in order`() {
        val targetAngles = (0..10 step 1).map { it.toDouble() }.toImmutableList()
        val plan = LimbMotionPlan(
            targetAngles.map {
                LimbMotionPlanStep(immutableListOf(it), createMotionConstraints(10))
            }.toImmutableList()
        )

        follower.followPlan(limb.jointAngleControllers, plan)

        assertEquals(targetAngles, controller.targets)
    }

    @Test
    fun `test plan length validation with empty plan`() {
        val plan = LimbMotionPlan(emptyImmutableList())
        follower.followPlan(limb.jointAngleControllers, plan) // Should not get an exception
    }

    @Test
    fun `test plan length validation with too few target joint angles`() {
        val plan = LimbMotionPlan(
            immutableListOf(
                LimbMotionPlanStep(
                    emptyImmutableList(),
                    createMotionConstraints(0)
                )
            )
        )

        assertThrows<IllegalArgumentException> {
            follower.followPlan(limb.jointAngleControllers, plan)
        }
    }

    @Test
    fun `test plan length validation with too few target joint angles part way down the list`() {
        val plan = LimbMotionPlan(
            immutableListOf(
                LimbMotionPlanStep(
                    immutableListOf(0.0),
                    createMotionConstraints(0)
                ),
                LimbMotionPlanStep(
                    emptyImmutableList(),
                    createMotionConstraints(0)
                )
            )
        )

        assertThrows<IllegalArgumentException> {
            follower.followPlan(limb.jointAngleControllers, plan)
        }
    }

    @Test
    fun `test plan length validation with too many target joint angles`() {
        val plan = LimbMotionPlan(
            immutableListOf(
                LimbMotionPlanStep(
                    immutableListOf(0.0, 0.0),
                    createMotionConstraints(0)
                )
            )
        )

        assertThrows<IllegalArgumentException> {
            follower.followPlan(limb.jointAngleControllers, plan)
        }
    }
}
