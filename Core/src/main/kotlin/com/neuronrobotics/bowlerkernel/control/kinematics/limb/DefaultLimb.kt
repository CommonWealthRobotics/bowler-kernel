/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.limb

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.limbid.LimbId
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.ForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.MotionConstraints
import com.neuronrobotics.bowlerkernel.scripting.GistScript
import com.neuronrobotics.bowlerkernel.util.emptyImmutableList
import com.neuronrobotics.bowlerkernel.util.toImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import kotlinx.coroutines.runBlocking

class DefaultLimb
internal constructor(
    override val id: LimbId,
    override val chain: ImmutableList<DhParam>,
    override val forwardKinematicsSolver: ForwardKinematicsSolver,
    override val inverseKinematicsSolver: InverseKinematicsSolver
) : Limb {

    override fun setDesiredTaskSpaceTransform(
        taskSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    ) {
        TODO("not implemented")
    }

    override fun getDesiredTaskSpaceTransform(): FrameTransformation {
        TODO("not implemented")
    }

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
        private val gistScriptFactory: GistScript.Factory
    ) : LimbFactory {

        override fun createLimb(limbData: LimbData) = DefaultLimb(
            id = limbData.id,
            chain = limbData.chain.map {
                DhParam(it.d, it.theta, it.r, it.alpha)
            }.toImmutableList(),
            forwardKinematicsSolver = gistScriptFactory.createGistScript(
                limbData.forwardKinematicsSolverGistId,
                limbData.forwardKinematicsSolverFilename
            ).runScript(emptyImmutableList()).fold(
                {
                    throw IllegalStateException(
                        """
                        |Cannot create ForwardKinematicsEngine:
                        |$it
                        """.trimMargin()
                    )
                },
                { it as ForwardKinematicsSolver }
            ),
            inverseKinematicsSolver = gistScriptFactory.createGistScript(
                limbData.inverseKinematicsSolverGistId,
                limbData.inverseKinematicsSolverFilename
            ).runScript(emptyImmutableList()).fold(
                {
                    throw IllegalStateException(
                        """
                        |Cannot create InverseKinematicsEngine:
                        |$it
                        """.trimMargin()
                    )
                },
                { it as InverseKinematicsSolver }
            )
        )
    }
}
