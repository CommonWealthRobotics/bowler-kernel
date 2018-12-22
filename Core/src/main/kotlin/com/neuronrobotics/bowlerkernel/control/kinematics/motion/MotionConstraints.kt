/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.motion

/**
 * Constraints on the motion of a movement.
 */
interface MotionConstraints {

    /**
     * The length of time, in seconds, the motion should be performed over.
     */
    val motionDuration: Number

    /**
     * The maximum velocity, in m/s, during the motion.
     */
    val maximumVelocity: Number

    /**
     * The maximum acceleration, in m/s/s, during the motion.
     */
    val maximumAcceleration: Number

    /**
     * The maximum jerk, in m/s/s/s, during the motion.
     */
    val maximumJerk: Number
}
