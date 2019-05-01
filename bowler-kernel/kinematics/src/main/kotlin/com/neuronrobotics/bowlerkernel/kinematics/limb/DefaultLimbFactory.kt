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

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.JointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkFactory
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbData
import com.neuronrobotics.bowlerkernel.kinematics.motion.ForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.InertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.scripting.factory.GitScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.getInstanceFromGit
import org.octogonapus.ktguava.collections.toImmutableList
import javax.inject.Inject

class DefaultLimbFactory
@Inject constructor(
    private val scriptFactory: GitScriptFactory,
    private val linkFactory: LinkFactory
) : LimbFactory {

    @SuppressWarnings("ReturnCount")
    override fun createLimb(limbData: LimbData): Either<LimbCreationError, Limb> {
        val links = limbData.links.map {
            val estimator = scriptFactory.getInstanceFromGit<InertialStateEstimator>(
                it.inertialStateEstimator
            ).fold({ return it.left() }, { it })

            linkFactory.createLink(
                it.type,
                it.dhParamData.toDhParam(),
                it.jointLimits,
                estimator
            )
        }.toImmutableList()

        val fkSolver = scriptFactory.getInstanceFromGit<ForwardKinematicsSolver>(
            limbData.forwardKinematicsSolver
        ).fold({ return it.left() }, { it })

        val ikSolver = scriptFactory.getInstanceFromGit<InverseKinematicsSolver>(
            limbData.inverseKinematicsSolver
        ).fold({ return it.left() }, { it })

        val limbMotionPlanGenerator = scriptFactory.getInstanceFromGit<LimbMotionPlanGenerator>(
            limbData.limbMotionPlanGenerator
        ).fold({ return it.left() }, { it })

        val limbMotionPlanFollower = scriptFactory.getInstanceFromGit<LimbMotionPlanFollower>(
            limbData.limbMotionPlanFollower
        ).fold({ return it.left() }, { it })

        val jointAngleControllers = limbData.links.map {
            scriptFactory.getInstanceFromGit<JointAngleController>(
                it.jointAngleController
            )
        }.map {
            it.fold({ return it.left() }, { it })
        }.toImmutableList()

        val inertialStateEstimator = scriptFactory.getInstanceFromGit<InertialStateEstimator>(
            limbData.inertialStateEstimator
        ).fold({ return it.left() }, { it })

        return DefaultLimb(
            SimpleLimbId(limbData.id),
            links,
            fkSolver,
            ikSolver,
            limbMotionPlanGenerator,
            limbMotionPlanFollower,
            jointAngleControllers,
            inertialStateEstimator
        ).right()
    }
}
