package com.neuronrobotics.bowlerkernel.kinematics.motion

import com.google.common.collect.ImmutableList

/**
 * An [InverseKinematicsSolver] which returns the given joint angles.
 */
object NoopInverseKinematicsSolver : InverseKinematicsSolver {

    override fun solveChain(
        currentJointAngles: ImmutableList<Double>,
        targetFrameTransform: FrameTransformation
    ) = currentJointAngles
}
