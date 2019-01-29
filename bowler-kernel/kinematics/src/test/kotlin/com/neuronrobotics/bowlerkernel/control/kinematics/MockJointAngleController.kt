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
package com.neuronrobotics.bowlerkernel.control.kinematics

import com.neuronrobotics.bowlerkernel.kinematics.closedloop.JointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints

internal class MockJointAngleController : JointAngleController {

    internal val times = mutableListOf<Long>()
    internal val targets = mutableListOf<Double>()

    override fun setTargetAngle(angle: Double, motionConstraints: MotionConstraints) {
        times.add(System.currentTimeMillis())
        targets.add(angle)
    }

    override fun getCurrentAngle() = targets.lastOrNull() ?: 0.0
}
