/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.kinematics.limb

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.JointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkFactory
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbData
import com.neuronrobotics.bowlerkernel.kinematics.motion.ForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.InertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.scripting.factory.GistScriptFactory
import com.neuronrobotics.bowlerkernel.util.emptyImmutableList
import com.neuronrobotics.bowlerkernel.util.toImmutableList
import javax.inject.Inject

class DefaultLimbFactory
@Inject constructor(
    private val scriptFactory: GistScriptFactory,
    private val linkFactory: LinkFactory
) : LimbFactory {

    @SuppressWarnings("ReturnCount")
    override fun createLimb(limbData: LimbData): Either<LimbCreationError, Limb> {
        val links = limbData.links.map {
            val estimator = getInstanceFromGist<InertialStateEstimator>(
                it.inertialStateEstimatorGistId,
                it.inertialStateEstimatorFilename
            ).fold({ return it.left() }, { it })

            linkFactory.createLink(
                it.type,
                it.dhParamData.toDhParam(),
                it.jointLimits,
                estimator
            )
        }.toImmutableList()

        val fkSolver = getInstanceFromGist<ForwardKinematicsSolver>(
            limbData.forwardKinematicsSolverGistId,
            limbData.forwardKinematicsSolverFilename
        ).fold({ return it.left() }, { it })

        val ikSolver = getInstanceFromGist<InverseKinematicsSolver>(
            limbData.inverseKinematicsSolverGistId,
            limbData.inverseKinematicsSolverFilename
        ).fold({ return it.left() }, { it })

        val limbMotionPlanGenerator = getInstanceFromGist<LimbMotionPlanGenerator>(
            limbData.limbMotionPlanGeneratorGistId,
            limbData.limbMotionPlanGeneratorFilename
        ).fold({ return it.left() }, { it })

        val limbMotionPlanFollower = getInstanceFromGist<LimbMotionPlanFollower>(
            limbData.limbMotionPlanFollowerGistId,
            limbData.limbMotionPlanFollowerFilename
        ).fold({ return it.left() }, { it })

        val jointAngleControllers = limbData.links.map {
            getInstanceFromGist<JointAngleController>(
                it.jointAngleControllerGistId,
                it.jointAngleControllerFilename
            )
        }.map {
            it.fold({ return it.left() }, { it })
        }.toImmutableList()

        val inertialStateEstimator = getInstanceFromGist<InertialStateEstimator>(
            limbData.inertialStateEstimatorGistId,
            limbData.inertialStateEstimatorFilename
        ).fold({ return it.left() }, { it })

        return DefaultLimb(
            limbData.id,
            links,
            fkSolver,
            ikSolver,
            limbMotionPlanGenerator,
            limbMotionPlanFollower,
            jointAngleControllers,
            inertialStateEstimator
        ).right()
    }

    private inline fun <reified T> getInstanceFromGist(gistId: String, filename: String) =
        scriptFactory.createScriptFromGist(gistId, filename).flatMap { script ->
            script.runScript(emptyImmutableList()).map { it as T }
        }
}
