/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.closedloop

import com.neuronrobotics.bowlerkernel.control.kinematics.base.KinematicBase
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.InertialState
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.MotionConstraints

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
