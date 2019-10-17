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
package com.neuronrobotics.bowlerkernel.kinematics

import com.neuronrobotics.bowlerkernel.kinematics.closedloop.LimbJointsController
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints
import com.neuronrobotics.bowlerkernel.util.JointLimits
import org.junit.jupiter.api.fail
import org.octogonapus.ktguava.collections.toImmutableList

class MockLimbJointsController(
    private val numLinks: Int
) : LimbJointsController {

    internal val times = mutableListOf<Long>()
    internal var targets = mutableListOf<List<Double>>()

    override val jointLimits = (1..numLinks).map { JointLimits.NoLimit }.toImmutableList()

    override fun setTargetAngles(angles: List<Double>, motionConstraints: MotionConstraints) {
        if (angles.size != numLinks) {
            fail { "Mismatched number of angles (${angles.size}) and links ($numLinks)" }
        }

        times.add(System.currentTimeMillis())
        targets.add(angles)
    }

    override fun getCurrentAngles() = targets.lastOrNull() ?: (1..numLinks).map { 0.0 }
}
