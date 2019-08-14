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
package com.neuronrobotics.bowlerkernel.kinematics.solvers.solver

import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.util.JointLimits
import com.neuronrobotics.bowlerkinematicsnative.solver.NativeIKSolver
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import java.nio.DoubleBuffer

/**
 * Solves any serial manipulator using a native solver.
 */
class NativeIKSolverBridge : InverseKinematicsSolver {

    @SuppressWarnings("ComplexMethod")
    override fun solveChain(
        links: List<Link>,
        currentJointAngles: List<Double>,
        jointLimits: List<JointLimits>,
        targetFrameTransform: FrameTransformation
    ): List<Double> {
        val numberOfLinks = links.size

        require(numberOfLinks > 0) {
            "Must have at least one link."
        }

        require(numberOfLinks == currentJointAngles.size) {
            """
            Links and joint angles must have equal length:
            Number of links: $numberOfLinks
            Number of joint angles: ${currentJointAngles.size}
            """.trimIndent()
        }

        require(numberOfLinks == jointLimits.size) {
            """
            Links and joint limits must have equal length:
            Number of links: $numberOfLinks
            Number of joint limits: ${jointLimits.size}
            """.trimIndent()
        }

        return NativeIKSolver.solve(
            numberOfLinks = numberOfLinks,
            dhParams = DoubleBuffer.allocate(numberOfLinks * 4).apply {
                links.forEach {
                    put(it.dhParam.d)
                    put(toRadians(it.dhParam.theta))
                    put(it.dhParam.r)
                    put(toRadians(it.dhParam.alpha))
                }

                rewind()
            }.array(),
            upperJointLimits = DoubleBuffer.allocate(numberOfLinks).apply {
                jointLimits.forEach { put(toRadians(it.maximum)) }
                rewind()
            }.array(),
            lowerJointLimits = DoubleBuffer.allocate(numberOfLinks).apply {
                jointLimits.forEach { put(toRadians(it.minimum)) }
                rewind()
            }.array(),
            initialJointAngles = DoubleBuffer.allocate(numberOfLinks).apply {
                currentJointAngles.forEach { put(toRadians(it)) }
                rewind()
            }.array(),
            target = DoubleBuffer.allocate(16).apply {
                targetFrameTransform.data.forEach { put(it) }
                rewind()
            }.array()
        ).map { toDegrees(it) }
    }
}
