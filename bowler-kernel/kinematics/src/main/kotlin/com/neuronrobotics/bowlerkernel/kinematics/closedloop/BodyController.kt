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
package com.neuronrobotics.bowlerkernel.kinematics.closedloop

import com.neuronrobotics.bowlerkernel.kinematics.base.KinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.InertialState
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints

/**
 * A closed-loop controller that can coordinate the limbs of a [KinematicBase].
 */
interface BodyController {

    /**
     * Sets a desired world space transform delta this base should try to move by.
     *
     * @param worldSpaceTransform The desired world space transform delta.
     * @param motionConstraints The constraints on the motion to move from the current world
     * space transform to the desired [worldSpaceTransform].
     */
    fun setDesiredWorldSpaceTransformDelta(
        worldSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    )

    /**
     * Returns the world space transform delta since the last set desired world space transform
     * delta. This is essentially the inverted error of the controller.
     *
     * @return The delta transform.
     */
    fun getDeltaSinceLastDesiredTransform(): FrameTransformation

    /**
     * Returns the current [InertialState] for this body.
     *
     * @return The current [InertialState].
     */
    fun getInertialState(): InertialState
}
