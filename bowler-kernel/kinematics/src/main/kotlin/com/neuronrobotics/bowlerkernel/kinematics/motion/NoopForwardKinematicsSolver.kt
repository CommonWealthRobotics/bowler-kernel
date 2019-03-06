package com.neuronrobotics.bowlerkernel.kinematics.motion

import com.google.common.collect.ImmutableList

/**
 * A [ForwardKinematicsSolver] which returns the identity frame transformation.
 */
object NoopForwardKinematicsSolver : ForwardKinematicsSolver {

    override fun solveChain(currentJointAngles: ImmutableList<Double>) =
        FrameTransformation.identity()
}
