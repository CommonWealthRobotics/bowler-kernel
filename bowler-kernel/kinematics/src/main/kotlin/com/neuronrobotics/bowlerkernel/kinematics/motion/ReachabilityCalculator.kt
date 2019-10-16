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

import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.util.JointLimits

/**
 * Determines whether a frame transform is reachable.
 */
interface ReachabilityCalculator {

    /**
     * Compute whether the [frameTransformation] is reachable.
     *
     * @param frameTransformation The task space transform to test.
     * @param links The links that make up the limb.
     * @param jointLimits The joint limits for each link.
     * @return Whether the [frameTransformation] is reachable.
     */
    fun isFrameTransformationReachable(
        frameTransformation: FrameTransformation,
        links: List<Link>,
        jointLimits: List<JointLimits>
    ): Boolean
}
