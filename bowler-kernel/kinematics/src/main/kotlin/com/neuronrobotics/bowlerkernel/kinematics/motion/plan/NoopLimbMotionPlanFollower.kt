package com.neuronrobotics.bowlerkernel.kinematics.motion.plan

/**
 * A [LimbMotionPlanFollower] which does nothing.
 */
object NoopLimbMotionPlanFollower : LimbMotionPlanFollower {

    override fun followPlan(plan: LimbMotionPlan) {
    }
}
