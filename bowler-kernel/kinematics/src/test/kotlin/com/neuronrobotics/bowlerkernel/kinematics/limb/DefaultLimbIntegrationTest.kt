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
package com.neuronrobotics.bowlerkernel.kinematics.limb

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.MockJointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.createMotionConstraints
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DefaultLink
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.kinematics.motion.ForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.LengthBasedReachabilityCalculator
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.DefaultLimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlan
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanStep
import com.neuronrobotics.bowlerkernel.util.Limits
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.toImmutableList
import kotlin.math.abs

internal class DefaultLimbIntegrationTest {

    @Test
    @Disabled("Doesn't perform well on CI servers.")
    fun `test limb plan execution`() {
        val timestep = 100

        val links = immutableListOf(
            DefaultLink(
                LinkType.Rotary, DhParam.zero,
                Limits(0.0, 0.0), mock {})
        )

        val controller = MockJointAngleController()

        val limb = DefaultLimb(
            SimpleLimbId(""),
            links.map { it as Link }.toImmutableList(),
            object : ForwardKinematicsSolver {
                override fun solveChain(
                    links: ImmutableList<Link>,
                    currentJointAngles: ImmutableList<Double>
                ): FrameTransformation {
                    return FrameTransformation.identity
                }
            },
            mock {},
            LengthBasedReachabilityCalculator(),
            object : LimbMotionPlanGenerator {
                override fun generatePlanForTaskSpaceTransform(
                    limb: Limb,
                    currentTaskSpaceTransform: FrameTransformation,
                    targetTaskSpaceTransform: FrameTransformation,
                    motionConstraints: MotionConstraints
                ): LimbMotionPlan {
                    return LimbMotionPlan(
                        immutableListOf(
                            LimbMotionPlanStep(
                                immutableListOf(0.0),
                                createMotionConstraints(
                                    timestep
                                )
                            ),
                            LimbMotionPlanStep(
                                immutableListOf(1.0),
                                createMotionConstraints(
                                    timestep
                                )
                            )
                        )
                    )
                }
            },
            DefaultLimbMotionPlanFollower(),
            immutableListOf(controller),
            mock {}
        )

        fun timeOnce(): Long {
            limb.setDesiredTaskSpaceTransform(
                FrameTransformation.identity,
                createMotionConstraints(timestep * 2)
            )

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
}
