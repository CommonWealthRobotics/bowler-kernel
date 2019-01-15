/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.limb

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.control.kinematics.MockJointAngleController
import com.neuronrobotics.bowlerkernel.control.createMotionConstraints
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.link.DefaultLink
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.ForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.MotionConstraints
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.plan.DefaultLimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.plan.LimbMotionPlan
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.plan.LimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.plan.LimbMotionPlanStep
import com.neuronrobotics.bowlerkernel.control.kinematics.Limits
import com.neuronrobotics.bowlerkernel.util.immutableListOf
import com.neuronrobotics.bowlerkernel.util.toImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs

internal class DefaultLimbIntegrationTest {

    @Test
    fun `test limb plan execution`() {
        val timestep = 100
        val links = immutableListOf(
            DefaultLink(LinkType.Rotary, DhParam.zero,
                Limits(0.0, 0.0), mock {})
        )
        val controller =
            MockJointAngleController()

        val limb = DefaultLimb(
            SimpleLimbId(""),
            links.map { it as Link }.toImmutableList(),
            object : ForwardKinematicsSolver {
                override fun solveChain(
                    currentJointAngles: ImmutableList<Double>
                ): FrameTransformation {
                    return FrameTransformation.identity()
                }
            },
            mock {},
            object : LimbMotionPlanGenerator {
                override fun generatePlanForTaskSpaceTransform(
                    currentTaskSpaceTransform: FrameTransformation,
                    targetTaskSpaceTransform: FrameTransformation,
                    motionConstraints: MotionConstraints
                ): LimbMotionPlan {
                    return LimbMotionPlan(
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
                }
            },
            DefaultLimbMotionPlanFollower(immutableListOf(controller)),
            immutableListOf(controller),
            mock {}
        )

        fun timeOnce(): Long {
            limb.setDesiredTaskSpaceTransform(
                FrameTransformation.identity(),
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
