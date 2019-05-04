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

/**
 * A solver that implements forward kinematics.
 */
interface ForwardKinematicsSolver {

    /**
     * Solve the system to produce its current task space frame transformation.
     *
     * @param links The links that make up the limb.
     * @param currentJointAngles The current joint angles.
     * @return The task space frame transformation of the chain given the [currentJointAngles].
     */
    fun solveChain(
        links: ImmutableList<Link>,
        currentJointAngles: ImmutableList<Double>
    ): FrameTransformation
}
