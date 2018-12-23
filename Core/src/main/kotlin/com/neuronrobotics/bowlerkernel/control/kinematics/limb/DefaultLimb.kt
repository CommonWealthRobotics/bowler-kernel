/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.limb

import arrow.core.Either
import arrow.core.flatMap
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.limbid.LimbId
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.ForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.LimbMotionPlanner
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.MotionConstraints
import com.neuronrobotics.bowlerkernel.scripting.DefaultScript
import com.neuronrobotics.bowlerkernel.util.emptyImmutableList
import com.neuronrobotics.bowlerkernel.util.toImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhParam

class DefaultLimb
internal constructor(
    override val id: LimbId,
    override val chain: ImmutableList<DhParam>,
    override val forwardKinematicsSolver: ForwardKinematicsSolver,
    override val inverseKinematicsSolver: InverseKinematicsSolver,
    override val motionPlanner: LimbMotionPlanner
) : Limb {

    // Start the desired task space transform at the home position
    private var desiredTaskSpaceTransform = forwardKinematicsSolver.solveChain(
        chain,
        chain.map { 0.0 }.toImmutableList()
    )

    override fun setDesiredTaskSpaceTransform(
        taskSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    ) {
        desiredTaskSpaceTransform = taskSpaceTransform
        val plan = motionPlanner.generatePlanForTaskSpaceTransform(
            chain,
            getCurrentTaskSpaceTransform(),
            taskSpaceTransform,
            motionConstraints
        )
        TODO("not implemented")
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
        TODO("not implemented")
    }

    override fun getCurrentJointAngles(): ImmutableList<Double> {
        TODO("not implemented")
    }

    override fun isTaskSpaceTransformReachable(taskSpaceTransform: FrameTransformation): Boolean {
        TODO("not implemented")
    }

    class Factory
    internal constructor(
        private val defaultScriptFactory: DefaultScript.Factory
    ) : LimbFactory {

        override fun createLimb(limbData: LimbData): Either<LimbCreationError, Limb> {
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
                            motionPlanner = motionPlanner
                        )
                    }
                }
            }
        }
    }
}
