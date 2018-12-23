/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.limb

import arrow.core.Either
import arrow.core.flatMap
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.control.closedloop.JointAngleController
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.limbid.LimbId
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.ForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.MotionConstraints
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.plan.LimbMotionPlanner
import com.neuronrobotics.bowlerkernel.scripting.DefaultScript
import com.neuronrobotics.bowlerkernel.util.emptyImmutableList
import com.neuronrobotics.bowlerkernel.util.toImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class DefaultLimb
internal constructor(
    override val id: LimbId,
    override val chain: ImmutableList<DhParam>,
    override val forwardKinematicsSolver: ForwardKinematicsSolver,
    override val inverseKinematicsSolver: InverseKinematicsSolver,
    override val motionPlanner: LimbMotionPlanner,
    override val jointAngleControllers: ImmutableList<JointAngleController>
) : Limb {

    // Start the desired task space transform at the home position
    private var desiredTaskSpaceTransform = forwardKinematicsSolver.solveChain(
        chain,
        chain.map { 0.0 }.toImmutableList()
    )

    init {
        require(chain.size == jointAngleControllers.size) {
            """
            |Must have an equal number of DH chain elements and joint angle controllers.
            |Chain size: ${chain.size}
            |Controllers size: ${jointAngleControllers.size}
            """.trimMargin()
        }
    }

    override fun setDesiredTaskSpaceTransform(
        taskSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    ) {
        desiredTaskSpaceTransform = taskSpaceTransform

        // Generate the plan on a new thread
        thread(isDaemon = true) {
            val plan = motionPlanner.generatePlanForTaskSpaceTransform(
                chain,
                getCurrentTaskSpaceTransform(),
                taskSpaceTransform,
                motionConstraints
            )

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
            }.fold(Unit) { _, elem ->
                // Wait for each step to finish
                elem.get()
            }
        }
    }

    override fun getDesiredTaskSpaceTransform() = desiredTaskSpaceTransform

    override fun getCurrentTaskSpaceTransform() =
        forwardKinematicsSolver.solveChain(
            chain,
            getCurrentJointAngles()
        )

    override fun setDesiredJointAngle(
        jointIndex: Int,
        jointAngle: Number,
        motionConstraints: MotionConstraints
    ) {
        jointAngleControllers[jointIndex].setTargetAngle(jointAngle.toDouble(), motionConstraints)
    }

    override fun getCurrentJointAngles() =
        jointAngleControllers.map { it.getCurrentAngle() }.toImmutableList()

    override fun isTaskSpaceTransformReachable(taskSpaceTransform: FrameTransformation): Boolean {
        TODO("not implemented")
    }

    class Factory
    internal constructor(
        private val defaultScriptFactory: DefaultScript.Factory
    ) : LimbFactory {

        override fun createLimb(limbData: LimbData) = createLimb(limbData, emptyImmutableList())

        fun createLimb(
            limbData: LimbData,
            jointAngleControllers: ImmutableList<JointAngleController>
        ): Either<LimbCreationError, Limb> {
            val fkSolver = defaultScriptFactory.createScriptFromGist(
                limbData.forwardKinematicsSolverGistId,
                limbData.forwardKinematicsSolverFilename
            ).flatMap {
                it.runScript(emptyImmutableList()).map { it as ForwardKinematicsSolver }
            }

            val ikSolver = defaultScriptFactory.createScriptFromGist(
                limbData.inverseKinematicsSolverGistId,
                limbData.inverseKinematicsSolverFilename
            ).flatMap {
                it.runScript(emptyImmutableList()).map { it as InverseKinematicsSolver }
            }

            val limbMotionPlanner = defaultScriptFactory.createScriptFromGist(
                limbData.limbMotionPlannerGistId,
                limbData.limbMotionPlannerFilename
            ).flatMap {
                it.runScript(emptyImmutableList()).map { it as LimbMotionPlanner }
            }

            return fkSolver.flatMap { fk ->
                ikSolver.flatMap { ik ->
                    limbMotionPlanner.map { motionPlanner ->
                        DefaultLimb(
                            id = limbData.id,
                            chain = limbData.chain.map {
                                DhParam(it.d, it.theta, it.r, it.alpha)
                            }.toImmutableList(),
                            forwardKinematicsSolver = fk,
                            inverseKinematicsSolver = ik,
                            motionPlanner = motionPlanner,
                            jointAngleControllers = jointAngleControllers
                        )
                    }
                }
            }
        }
    }
}
