/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.closedloop

import com.neuronrobotics.bowlerkernel.control.kinematics.motion.MotionConstraints

/**
 * A temporary closed-loop joint angle controller.
 * //TODO: Turn this into a larger closed-loop controller API
 */
interface JointAngleController {

    /**
     * Move to the target [angle].
     *
     * @param angle The target joint angle.
     * @param motionConstraints The constraints on the motion to move from the current joint
     * angle to the target [angle].
     */
    fun setTargetAngle(angle: Double, motionConstraints: MotionConstraints)

    fun getCurrentAngle(): Double
}
