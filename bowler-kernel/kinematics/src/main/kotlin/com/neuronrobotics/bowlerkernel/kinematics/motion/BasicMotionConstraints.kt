/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.kinematics.motion

data class BasicMotionConstraints(
    override val motionDuration: Number,
    override val maximumVelocity: Number,
    override val maximumAcceleration: Number,
    override val maximumJerk: Number
) : MotionConstraints
