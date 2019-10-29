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
import com.neuronrobotics.bowlerkernel.kinematics.base.KinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.LimbJointsController
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.motion.ForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.InertialState
import com.neuronrobotics.bowlerkernel.kinematics.motion.InertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints
import com.neuronrobotics.bowlerkernel.kinematics.motion.ReachabilityCalculator
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanGenerator

/**
 * A limb of a robot, mounted to a [KinematicBase].
 */
@SuppressWarnings("ComplexInterface")
interface Limb {

    /**
     * The unique (per-[KinematicBase]) id of this limb.
     */
    val id: String

    /**
     * The links that form this limb.
     */
    val links: ImmutableList<Link>

    /**
     * The solver used for forward kinematics.
     */
    val forwardKinematicsSolver: ForwardKinematicsSolver

    /**
     * The solver used for inverse kinematics.
     */
    val inverseKinematicsSolver: InverseKinematicsSolver

    /**
     * Used to compute whether a target frame transform is reachable.
     */
    val reachabilityCalculator: ReachabilityCalculator

    /**
     * The motion planner used to generate plans for the limb to follow.
     */
    val motionPlanGenerator: LimbMotionPlanGenerator

    /**
     * The motion plan follower used to follow plans generated by [motionPlanGenerator].
     */
    val motionPlanFollower: LimbMotionPlanFollower

    /**
     * The controller for the joints.
     */
    val jointsController: LimbJointsController

    /**
     * The [InertialStateEstimator].
     */
    val inertialStateEstimator: InertialStateEstimator

    /**
     * Sets a desired task space transform this limb should try to move to.
     *
     * @param taskSpaceTransform The desired task space transform.
     * @param motionConstraints The constraints on the motion to move from the current task
     * space transform to the desired [taskSpaceTransform].
     */
    fun setDesiredTaskSpaceTransform(
        taskSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    )

    /**
     * The last set desired task space transform this limb should try to move to.
     */
    fun getDesiredTaskSpaceTransform(): FrameTransformation

    /**
     * The current task space transform.
     */
    fun getCurrentTaskSpaceTransform(): FrameTransformation

    /**
     * Whether the limb is currently moving to the desired task space transform.
     */
    fun isMovingToTaskSpaceTransform(): Boolean

    /**
     * Sets new desired joint angles.
     *
     * @param jointAngles The new joint angles.
     * @param motionConstraints The constraints of the motion to move from the current joint
     * angles to the desired [jointAngles].
     */
    fun setDesiredJointAngle(jointAngles: List<Double>, motionConstraints: MotionConstraints)

    /**
     * The current joint angles.
     */
    fun getCurrentJointAngles(): List<Double>

    /**
     * Compute whether the [taskSpaceTransform] is reachable by this limb.
     *
     * @param taskSpaceTransform The task space transform to test.
     * @return Whether the [taskSpaceTransform] is reachable.
     */
    fun isTaskSpaceTransformReachable(taskSpaceTransform: FrameTransformation): Boolean

    /**
     * Returns the current [InertialState] for this limb.
     *
     * @return The current [InertialState].
     */
    fun getInertialState(): InertialState
}
