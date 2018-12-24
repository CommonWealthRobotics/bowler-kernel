/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.limb

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.control.closedloop.JointAngleController
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.BasicMotionConstraints
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.ForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.MotionConstraints
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.plan.LimbMotionPlan
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.plan.LimbMotionPlanStep
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.plan.LimbMotionPlanner
import com.neuronrobotics.bowlerkernel.util.immutableListOf
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs

internal class DefaultLimbTest {

    private fun createMotionConstraints(duration: Number) = BasicMotionConstraints(
        duration, 0, 0, 0
    )

    @Test
    fun `test limb plan execution`() {
        val timestep = 20
        val chain = immutableListOf(DhParam.zero)
        val controller = object : JointAngleController {
            val times = mutableListOf<Long>()
            val targets = mutableListOf<Double>()

            override fun setTargetAngle(angle: Double, motionConstraints: MotionConstraints) {
                times.add(System.currentTimeMillis())
                targets.add(angle)
            }

            override fun getCurrentAngle(): Double {
                return 0.0
            }
        }
        val limb = DefaultLimb(
            SimpleLimbId(""),
            chain,
            object : ForwardKinematicsSolver {
                override fun solveChain(
                    chain: ImmutableList<DhParam>,
                    currentJointAngles: ImmutableList<Double>
                ): FrameTransformation {
                    return FrameTransformation.identity(4)
                }
            },
            object : InverseKinematicsSolver {
                override fun solveChain(
                    chain: ImmutableList<DhParam>,
                    currentJointAngles: ImmutableList<Double>,
                    targetFrameTransform: FrameTransformation
                ): ImmutableList<Double> {
                    TODO("not implemented")
                }
            },
            object : LimbMotionPlanner {
                override fun generatePlanForTaskSpaceTransform(
                    chain: ImmutableList<DhParam>,
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
            immutableListOf(controller)
        )

        fun timeOnce(): Long {
            limb.setDesiredTaskSpaceTransform(
                FrameTransformation.identity(4),
                createMotionConstraints(timestep * 2)
            )

            runBlocking { delay(100) }

            val time = controller.times[1] - controller.times[0]

            controller.times.clear()
            controller.targets.clear()

            return time
        }

        val averageTiming = (1..10).toList().map {
            timeOnce()
        }.average()

        assertTrue(abs(averageTiming - timestep) < 2) {
            "The average timing was too large: $averageTiming"
        }
    }
}
