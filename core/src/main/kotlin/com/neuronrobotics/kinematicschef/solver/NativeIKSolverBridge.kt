/*
 * This file is part of kinematics-chef.
 *
 * kinematics-chef is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kinematics-chef is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with kinematics-chef.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.kinematicschef.solver

import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.util.JointLimits
import com.neuronrobotics.bowlerkinematicsnative.solver.NativeIKSolver
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import java.nio.FloatBuffer

class NativeIKSolverBridge : InverseKinematicsSolver {

    override fun solveChain(
        links: List<Link>,
        currentJointAngles: List<Double>,
        jointLimits: List<JointLimits>,
        targetFrameTransform: FrameTransformation
    ): List<Double> {
        val buf = FloatBuffer.allocate(links.size * 7 + 16).apply {
            links.forEach {
                put(it.dhParam.d.toFloat())
                put(toRadians(it.dhParam.theta).toFloat())
                put(it.dhParam.r.toFloat())
                put(toRadians(it.dhParam.alpha).toFloat())
            }

            jointLimits.forEach {
                put(toRadians(it.maximum).toFloat())
            }

            jointLimits.forEach {
                put(toRadians(it.minimum).toFloat())
            }

            targetFrameTransform.data.forEach {
                put(it.toFloat())
            }

            currentJointAngles.forEach {
                put(toRadians(it).toFloat())
            }

            rewind()
        }

        return NativeIKSolver.solve(links.size, buf.array()).map { toDegrees(it.toDouble()) }
    }
}
