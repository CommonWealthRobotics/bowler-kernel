/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.LimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.util.toImmutableList
import com.neuronrobotics.kinematicschef.dhparam.toFrameTransformation
import com.neuronrobotics.kinematicschef.util.getTranslation
import com.neuronrobotics.kinematicschef.util.length
import kotlin.concurrent.thread

class DefaultLimb(
    override val id: LimbId,
    override val links: ImmutableList<Link>,
    override val forwardKinematicsSolver: ForwardKinematicsSolver,
    override val inverseKinematicsSolver: InverseKinematicsSolver,
    override val motionPlanGenerator: LimbMotionPlanGenerator,
    override val motionPlanFollower: LimbMotionPlanFollower,
    override val jointAngleControllers: ImmutableList<JointAngleController>,
    override val inertialStateEstimator: InertialStateEstimator
) : Limb {

    // Start the desired task space transform at the home position
    private var desiredTaskSpaceTransform = forwardKinematicsSolver.solveChain(
        links.map { 0.0 }.toImmutableList()
    )

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
        thread(isDaemon = true) {
            val plan = motionPlanGenerator.generatePlanForTaskSpaceTransform(
                getCurrentTaskSpaceTransform(),
                taskSpaceTransform,
                motionConstraints
            )

            motionPlanFollower.followPlan(plan)
        }
    }

    override fun getDesiredTaskSpaceTransform() = desiredTaskSpaceTransform

    override fun getCurrentTaskSpaceTransform() =
        forwardKinematicsSolver.solveChain(getCurrentJointAngles())

    override fun setDesiredJointAngle(
        jointIndex: Int,
        jointAngle: Number,
        motionConstraints: MotionConstraints
    ) = jointAngleControllers[jointIndex].setTargetAngle(jointAngle.toDouble(), motionConstraints)

    override fun getCurrentJointAngles() =
        jointAngleControllers.map { it.getCurrentAngle() }.toImmutableList()

    override fun isTaskSpaceTransformReachable(taskSpaceTransform: FrameTransformation) =
        taskSpaceTransform.getTranslation().length() <
            links.map { it.dhParam }.toFrameTransformation().getTranslation().length()

    override fun getInertialState() = inertialStateEstimator.getInertialState()
}
