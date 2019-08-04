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
package com.neuronrobotics.bowlerkernel.kinematics.factories

import arrow.core.Either
import arrow.core.extensions.either.monad.binding
import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.JointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.limb.DefaultLimb
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.kinematics.limb.LimbFactory
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkFactory
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbScriptData
import com.neuronrobotics.bowlerkernel.kinematics.motion.ForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.InertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.ReachabilityCalculator
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.scripting.factory.GitScriptFactory
import org.octogonapus.ktguava.collections.toImmutableList

class DefaultLimbFactory(
    private val scriptFactory: GitScriptFactory,
    private val linkFactory: LinkFactory,
    private val klaxon: Klaxon = Klaxon()
) : LimbFactory {

    @SuppressWarnings("ReturnCount")
    override fun createLimb(
        limbConfigurationData: LimbConfigurationData,
        limbScriptData: LimbScriptData
    ): Either<String, Limb> = binding {
        require(limbConfigurationData.linkConfigurations.size == limbScriptData.linkScripts.size) {
            """
            Must have an equal number of link configurations and link scripts.
            Link configurations size: ${limbConfigurationData.linkConfigurations.size}
            Link scripts size: ${limbScriptData.linkScripts.size}
            """.trimIndent()
        }

        val links = limbConfigurationData.linkConfigurations
            .zip(limbScriptData.linkScripts)
            .map { linkFactory.createLink(it.first, it.second).bind() }
            .toImmutableList()

        val (fkSolver) = limbScriptData.forwardKinematicsSolver
            .createInstance<ForwardKinematicsSolver>(scriptFactory, klaxon)

        val (ikSolver) = limbScriptData.inverseKinematicsSolver
            .createInstance<InverseKinematicsSolver>(scriptFactory, klaxon)

        val (reachabilityCalculator) = limbScriptData.reachabilityCalculator
            .createInstance<ReachabilityCalculator>(scriptFactory, klaxon)

        val (limbMotionPlanGenerator) = limbScriptData.limbMotionPlanGenerator
            .createInstance<LimbMotionPlanGenerator>(scriptFactory, klaxon)

        val (limbMotionPlanFollower) = limbScriptData.limbMotionPlanFollower
            .createInstance<LimbMotionPlanFollower>(scriptFactory, klaxon)

        val jointAngleControllers = limbScriptData.linkScripts.map {
            it.jointAngleController
                .createInstance<JointAngleController>(scriptFactory, klaxon)
                .bind()
        }.toImmutableList()

        val (inertialStateEstimator) = limbScriptData.inertialStateEstimator
            .createInstance<InertialStateEstimator>(scriptFactory, klaxon)

        DefaultLimb(
            SimpleLimbId(limbConfigurationData.id),
            links,
            fkSolver,
            ikSolver,
            reachabilityCalculator,
            limbMotionPlanGenerator,
            limbMotionPlanFollower,
            jointAngleControllers,
            inertialStateEstimator
        )
    }
}
