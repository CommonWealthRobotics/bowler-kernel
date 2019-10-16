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
package com.neuronrobotics.bowlerkernel.kinematics.motion

import arrow.effects.IO
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.toFrameTransformation
import com.neuronrobotics.bowlerkernel.util.JointLimits

/**
 * Uses a [LengthBasedReachabilityCalculator] and an [InverseKinematicsSolver] to determine if
 * a [FrameTransformation] is reachable.
 */
class LengthAndIKBasedReachabilityCalculator(
    private val inverseKinematicsSolver: InverseKinematicsSolver,
    private val lengthBasedReachabilityCalculator: LengthBasedReachabilityCalculator =
        LengthBasedReachabilityCalculator()
) : ReachabilityCalculator {

    override fun isFrameTransformationReachable(
        frameTransformation: FrameTransformation,
        links: List<Link>,
        jointLimits: List<JointLimits>
    ): Boolean = lengthBasedReachabilityCalculator.isFrameTransformationReachable(
        frameTransformation,
        links,
        jointLimits
    ) && IO {
        inverseKinematicsSolver.solveChain(
            links,
            links.map { 0.0 },
            jointLimits,
            frameTransformation
        )
    }.attempt().unsafeRunSync().isRight()
}
