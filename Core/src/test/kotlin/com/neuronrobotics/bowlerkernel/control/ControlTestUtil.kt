/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control

import com.neuronrobotics.bowlerkernel.control.kinematics.motion.BasicMotionConstraints

internal fun createMotionConstraints(duration: Number) = BasicMotionConstraints(
    duration, 0, 0, 0
)
