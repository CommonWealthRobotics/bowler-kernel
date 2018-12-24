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
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.plan.LimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.plan.LimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.scripting.DefaultScript
import com.neuronrobotics.bowlerkernel.util.emptyImmutableList
import com.neuronrobotics.bowlerkernel.util.toImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.toFrameTransformation
import com.neuronrobotics.kinematicschef.util.getTranslation
import com.neuronrobotics.kinematicschef.util.length
import kotlin.concurrent.thread

class DefaultLimb
internal constructor(
    override val id: LimbId,
    override val chain: ImmutableList<DhParam>,
    override val forwardKinematicsSolver: ForwardKinematicsSolver,
    override val inverseKinematicsSolver: InverseKinematicsSolver,
    override val motionPlanGenerator: LimbMotionPlanGenerator,
    override val motionPlanFollower: LimbMotionPlanFollower,
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

        // Generate and follow the plan on a new thread
        thread(isDaemon = true) {
            val plan = motionPlanGenerator.generatePlanForTaskSpaceTransform(
                chain,
                getCurrentTaskSpaceTransform(),
                taskSpaceTransform,
                motionConstraints
            )

            motionPlanFollower.followPlan(plan)
        }
    }

    override fun getDesiredTaskSpaceTransform() = desiredTaskSpaceTransform

    override fun getCurrentTaskSpaceTransform() =
        forwardKinematicsSolver.solveChain(chain, getCurrentJointAngles())

    override fun setDesiredJointAngle(
        jointIndex: Int,
        jointAngle: Number,
        motionConstraints: MotionConstraints
    ) {
        jointAngleControllers[jointIndex].setTargetAngle(jointAngle.toDouble(), motionConstraints)
    }

    override fun getCurrentJointAngles() =
        jointAngleControllers.map { it.getCurrentAngle() }.toImmutableList()

    override fun isTaskSpaceTransformReachable(taskSpaceTransform: FrameTransformation) =
        taskSpaceTransform.getTranslation().length() <
            chain.toFrameTransformation().getTranslation().length()

    class Factory
    internal constructor(
        private val defaultScriptFactory: DefaultScript.Factory
    ) : LimbFactory {

        override fun createLimb(limbData: LimbData) = createLimb(limbData, emptyImmutableList())

        fun createLimb(
            limbData: LimbData,
            jointAngleControllers: ImmutableList<JointAngleController>
        ): Either<LimbCreationError, Limb> {
            val fkSolver = getInstanceFromGist<ForwardKinematicsSolver>(
                limbData.forwardKinematicsSolverGistId,
                limbData.forwardKinematicsSolverFilename
            )

            val ikSolver = getInstanceFromGist<InverseKinematicsSolver>(
                limbData.inverseKinematicsSolverGistId,
                limbData.inverseKinematicsSolverFilename
            )

            val limbMotionPlanGenerator = getInstanceFromGist<LimbMotionPlanGenerator>(
                limbData.limbMotionPlanGeneratorGistId,
                limbData.limbMotionPlanGeneratorFilename
            )

            val limbMotionPlanFollower = getInstanceFromGist<LimbMotionPlanFollower>(
                limbData.limbMotionPlanFollowerGistId,
                limbData.limbMotionPlanFollowerFilename
            )

            return fkSolver.flatMap { fk ->
                ikSolver.flatMap { ik ->
                    limbMotionPlanGenerator.flatMap { generator ->
                        limbMotionPlanFollower.map { follower ->
                            DefaultLimb(
                                id = limbData.id,
                                chain = limbData.chain.map {
                                    DhParam(it.d, it.theta, it.r, it.alpha)
                                }.toImmutableList(),
                                forwardKinematicsSolver = fk,
                                inverseKinematicsSolver = ik,
                                motionPlanGenerator = generator,
                                motionPlanFollower = follower,
                                jointAngleControllers = jointAngleControllers
                            )
                        }
                    }
                }
            }
        }

        private inline fun <reified T> getInstanceFromGist(gistId: String, filename: String) =
            defaultScriptFactory.createScriptFromGist(gistId, filename).flatMap {
                it.runScript(emptyImmutableList()).map { it as T }
            }
    }
}
