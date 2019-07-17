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

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.toFrameTransformation

/**
 * A simple implementation using only the distance to the transform from the origin and the
 * length of the limb.
 */
class LengthBasedReachabilityCalculator : ReachabilityCalculator {

    override fun isFrameTransformationReachable(
        frameTransformation: FrameTransformation,
        links: ImmutableList<Link>
    ) = frameTransformation.translation.length() <=
        links.map { it.dhParam }.toFrameTransformation().translation.length()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LengthBasedReachabilityCalculator) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
