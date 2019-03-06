package com.neuronrobotics.bowlerkernel.kinematics.motion.plan

import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints
import org.octogonapus.guavautil.collections.emptyImmutableList

/**
 * A [LimbMotionPlanGenerator] which generates an empty plan.
 */
object NoopLimbMotionPlanGenerator : LimbMotionPlanGenerator {

    override fun generatePlanForTaskSpaceTransform(
        currentTaskSpaceTransform: FrameTransformation,
        targetTaskSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    ) = LimbMotionPlan(emptyImmutableList())
}
