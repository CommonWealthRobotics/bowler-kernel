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

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.JointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.LimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.motion.ForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.InertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints
import com.neuronrobotics.bowlerkernel.kinematics.motion.ReachabilityCalculator
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanGenerator
import java.util.concurrent.Executors
import org.octogonapus.ktguava.collections.toImmutableList

class DefaultLimb(
    override val id: LimbId,
    override val links: ImmutableList<Link>,
    override val forwardKinematicsSolver: ForwardKinematicsSolver,
    override val inverseKinematicsSolver: InverseKinematicsSolver,
    override val reachabilityCalculator: ReachabilityCalculator,
    override val motionPlanGenerator: LimbMotionPlanGenerator,
    override val motionPlanFollower: LimbMotionPlanFollower,
    override val jointAngleControllers: ImmutableList<JointAngleController>,
    override val inertialStateEstimator: InertialStateEstimator
) : Limb {

    // Start the desired task space transform at the home position
    private var desiredTaskSpaceTransform = forwardKinematicsSolver.solveChain(
        links,
        links.map { 0.0 }.toImmutableList()
    )

    private var movingToTaskSpaceTransform = false
    private val moveLimbPool = Executors.newSingleThreadExecutor()

    init {
        require(links.size == jointAngleControllers.size) {
            """
            |Must have an equal number of DH chain elements and joint angle controllers.
            |Chain size: ${links.size}
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
        movingToTaskSpaceTransform = true
        moveLimbPool.submit {
            try {
                val plan = motionPlanGenerator.generatePlanForTaskSpaceTransform(
                    this,
                    getCurrentTaskSpaceTransform(),
                    taskSpaceTransform,
                    motionConstraints
                )

                motionPlanFollower.followPlan(jointAngleControllers, plan)
            } finally {
                movingToTaskSpaceTransform = false
            }
        }
    }

    override fun getDesiredTaskSpaceTransform() = desiredTaskSpaceTransform

    override fun getCurrentTaskSpaceTransform() =
        forwardKinematicsSolver.solveChain(links, getCurrentJointAngles())

    override fun isMovingToTaskSpaceTransform() = movingToTaskSpaceTransform

    override fun setDesiredJointAngle(
        jointIndex: Int,
        jointAngle: Number,
        motionConstraints: MotionConstraints
    ) = jointAngleControllers[jointIndex].setTargetAngle(jointAngle.toDouble(), motionConstraints)

    override fun getCurrentJointAngles() =
        jointAngleControllers.map { it.getCurrentAngle() }.toImmutableList()

    override fun isTaskSpaceTransformReachable(taskSpaceTransform: FrameTransformation) =
        reachabilityCalculator.isFrameTransformationReachable(taskSpaceTransform, links)

    override fun getInertialState() = inertialStateEstimator.getInertialState()

    @SuppressWarnings("ComplexMethod")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefaultLimb) return false

        if (id != other.id) return false
        if (links != other.links) return false
        if (forwardKinematicsSolver != other.forwardKinematicsSolver) return false
        if (inverseKinematicsSolver != other.inverseKinematicsSolver) return false
        if (reachabilityCalculator != other.reachabilityCalculator) return false
        if (motionPlanGenerator != other.motionPlanGenerator) return false
        if (motionPlanFollower != other.motionPlanFollower) return false
        if (jointAngleControllers != other.jointAngleControllers) return false
        if (inertialStateEstimator != other.inertialStateEstimator) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + links.hashCode()
        result = 31 * result + forwardKinematicsSolver.hashCode()
        result = 31 * result + inverseKinematicsSolver.hashCode()
        result = 31 * result + reachabilityCalculator.hashCode()
        result = 31 * result + motionPlanGenerator.hashCode()
        result = 31 * result + motionPlanFollower.hashCode()
        result = 31 * result + jointAngleControllers.hashCode()
        result = 31 * result + inertialStateEstimator.hashCode()
        return result
    }
}
