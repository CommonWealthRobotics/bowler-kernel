/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control

import com.neuronrobotics.bowlerkernel.control.closedloop.JointAngleController
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.MotionConstraints

internal class MockJointAngleController : JointAngleController {

    internal val times = mutableListOf<Long>()
    internal val targets = mutableListOf<Double>()

    override fun setTargetAngle(angle: Double, motionConstraints: MotionConstraints) {
        times.add(System.currentTimeMillis())
        targets.add(angle)
    }

    override fun getCurrentAngle() = targets.lastOrNull() ?: 0.0
}
