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
package com.neuronrobotics.bowlerkernel.oldstackconverter

import arrow.core.left
import arrow.core.right
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.base.DefaultKinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.base.KinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.KinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.SimpleKinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.BodyController
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.JointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.graph.BaseNode
import com.neuronrobotics.bowlerkernel.kinematics.graph.KinematicGraph
import com.neuronrobotics.bowlerkernel.kinematics.graph.buildMutableKinematicGraph
import com.neuronrobotics.bowlerkernel.kinematics.limb.DefaultLimb
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.LimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DefaultLink
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.kinematics.motion.ForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.InertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.LengthBasedReachabilityCalculator
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.kinematics.solvers.GeneralForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.solvers.GeneralInverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.util.JointLimits
import com.neuronrobotics.sdk.addons.kinematics.DHLink
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
import org.octogonapus.ktguava.collections.toImmutableList
import org.octogonapus.ktguava.collections.toImmutableNetwork
import java.lang.Math.toDegrees

@Suppress("UnstableApiUsage")
class RobotConverter(
    private val mobileBase: MobileBase
) {

    private val baseId by lazy {
        SimpleKinematicBaseId(mobileBase.scriptingName)
    }

    fun convertToKinematicBase(
        bodyController: BodyController,
        limbFKSolver: (KinematicBaseId, LimbId) -> ForwardKinematicsSolver = defaultFKSolver,
        limbIKSolver: (KinematicBaseId, LimbId, ImmutableList<Link>) -> InverseKinematicsSolver = defaultIKSolver,
        limbMotionPlanGeneratorFactory: (KinematicBaseId, LimbId) -> LimbMotionPlanGenerator,
        limbMotionPlanFollowerFactory: (KinematicBaseId, LimbId) -> LimbMotionPlanFollower,
        jointAngleControllerFactory: (KinematicBaseId, LimbId, Int, JointLimits) -> JointAngleController,
        linkInertialStateEstimatorFactory: (KinematicBaseId, LimbId, Int) -> InertialStateEstimator,
        limbInertialStateEstimatorFactory: (KinematicBaseId, LimbId) -> InertialStateEstimator
    ): KinematicBase {
        return DefaultKinematicBase.create(
            kinematicGraph = convertToKinematicGraph(
                limbFKSolver = limbFKSolver,
                limbIKSolver = limbIKSolver,
                limbMotionPlanGeneratorFactory = limbMotionPlanGeneratorFactory,
                limbMotionPlanFollowerFactory = limbMotionPlanFollowerFactory,
                jointAngleControllerFactory = jointAngleControllerFactory,
                linkInertialStateEstimatorFactory = linkInertialStateEstimatorFactory,
                limbInertialStateEstimatorFactory = limbInertialStateEstimatorFactory
            ),
            baseId = baseId,
            bodyController = bodyController
        )
    }

    fun convertToKinematicGraph(
        limbFKSolver: (KinematicBaseId, LimbId) -> ForwardKinematicsSolver = defaultFKSolver,
        limbIKSolver: (KinematicBaseId, LimbId, ImmutableList<Link>) -> InverseKinematicsSolver = defaultIKSolver,
        limbMotionPlanGeneratorFactory: (KinematicBaseId, LimbId) -> LimbMotionPlanGenerator,
        limbMotionPlanFollowerFactory: (KinematicBaseId, LimbId) -> LimbMotionPlanFollower,
        jointAngleControllerFactory: (KinematicBaseId, LimbId, Int, JointLimits) -> JointAngleController,
        linkInertialStateEstimatorFactory: (KinematicBaseId, LimbId, Int) -> InertialStateEstimator,
        limbInertialStateEstimatorFactory: (KinematicBaseId, LimbId) -> InertialStateEstimator
    ): KinematicGraph {
        // TODO: Handle parallel groups
        require(mobileBase.allParallelGroups.all { it.constituantLimbs.isEmpty() }) {
            "This converter cannot convert parallel groups."
        }

        val mutableKinematicGraph = buildMutableKinematicGraph()
        val baseNode = BaseNode(baseId).left()

        val limbsWithBaseFTs = mobileBase.allDHChains.map {
            val limbId = SimpleLimbId(it.scriptingName)

            mapLimb(
                oldLimb = it,
                limbFKSolver = limbFKSolver,
                limbIKSolver = limbIKSolver,
                linkInertialStateEstimatorFactory = linkInertialStateEstimatorFactory,
                limbMotionPlanGenerator = limbMotionPlanGeneratorFactory(baseId, limbId),
                limbMotionPlanFollower = limbMotionPlanFollowerFactory(baseId, limbId),
                jointAngleControllerFactory = jointAngleControllerFactory,
                limbInertialStateEstimator = limbInertialStateEstimatorFactory(baseId, limbId)
            )
        }

        limbsWithBaseFTs.forEach { (limb, baseFT) ->
            mutableKinematicGraph.addEdge(baseNode, limb.right(), baseFT)
        }

        return mutableKinematicGraph.toImmutableNetwork()
    }

    private fun mapLimb(
        oldLimb: DHParameterKinematics,
        linkInertialStateEstimatorFactory: (KinematicBaseId, LimbId, Int) -> InertialStateEstimator,
        limbFKSolver: (KinematicBaseId, LimbId) -> ForwardKinematicsSolver = defaultFKSolver,
        limbIKSolver: (KinematicBaseId, LimbId, ImmutableList<Link>) -> InverseKinematicsSolver = defaultIKSolver,
        limbMotionPlanGenerator: LimbMotionPlanGenerator,
        limbMotionPlanFollower: LimbMotionPlanFollower,
        jointAngleControllerFactory: (KinematicBaseId, LimbId, Int, JointLimits) -> JointAngleController,
        limbInertialStateEstimator: InertialStateEstimator
    ): Pair<DefaultLimb, FrameTransformation> {
        val limbId = SimpleLimbId(oldLimb.scriptingName)

        val links: ImmutableList<Link> = oldLimb.chain.links.mapIndexed { index, link ->
            mapLink(link, linkInertialStateEstimatorFactory, limbId, index)
        }.toImmutableList()

        val limits = oldLimb.chain.upperLimits
            .zip(oldLimb.chain.getlowerLimits())
            .map { (maximum, minimum) ->
                mapJointLimits(maximum, minimum)
            }

        val limb = DefaultLimb(
            limbId,
            links,
            limbFKSolver(baseId, limbId),
            limbIKSolver(baseId, limbId, links),
            LengthBasedReachabilityCalculator(),
            limbMotionPlanGenerator,
            limbMotionPlanFollower,
            links.zip(limits).mapIndexed { index, pair ->
                jointAngleControllerFactory(baseId, limbId, index, pair.second)
            }.toImmutableList(),
            limbInertialStateEstimator
        )

        val baseFT =
            FrameTransformation.fromMatrix(oldLimb.robotToFiducialTransform.matrixTransform)

        return limb to baseFT
    }

    private fun mapJointLimits(
        maximum: Double,
        minimum: Double
    ) = JointLimits(maximum = maximum, minimum = minimum)

    private fun mapLink(
        link: DHLink,
        linkInertialStateEstimatorFactory: (KinematicBaseId, LimbId, Int) -> InertialStateEstimator,
        limbId: SimpleLimbId,
        index: Int
    ) = DefaultLink(
        LinkType.Rotary,
        DhParam(link.d, toDegrees(link.theta), link.r, toDegrees(link.alpha)),
        linkInertialStateEstimatorFactory(baseId, limbId, index)
    )

    companion object {
        private val defaultFKSolver: (KinematicBaseId, LimbId) -> GeneralForwardKinematicsSolver =
            { _, _ -> GeneralForwardKinematicsSolver() }

        private val defaultIKSolver: (KinematicBaseId, LimbId, ImmutableList<Link>) -> GeneralInverseKinematicsSolver =
            { _, _, links -> GeneralInverseKinematicsSolver(links) }
    }
}
