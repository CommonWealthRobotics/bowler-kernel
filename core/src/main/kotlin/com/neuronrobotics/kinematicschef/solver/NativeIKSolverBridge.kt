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
            dhParams = FloatBuffer.allocate(numberOfLinks * 4).apply {
                links.forEach {
                    put(it.dhParam.d.toFloat())
                    put(toRadians(it.dhParam.theta).toFloat())
                    put(it.dhParam.r.toFloat())
                    put(toRadians(it.dhParam.alpha).toFloat())
                }

                rewind()
            }.array(),
            upperJointLimits = FloatBuffer.allocate(numberOfLinks).apply {
                jointLimits.forEach { put(toRadians(it.maximum).toFloat()) }
                rewind()
            }.array(),
            lowerJointLimits = FloatBuffer.allocate(numberOfLinks).apply {
                jointLimits.forEach { put(toRadians(it.minimum).toFloat()) }
                rewind()
            }.array(),
            initialJointAngles = FloatBuffer.allocate(numberOfLinks).apply {
                currentJointAngles.forEach { put(toRadians(it).toFloat()) }
                rewind()
            }.array(),
            target = FloatBuffer.allocate(16).apply {
                targetFrameTransform.data.forEach { put(it.toFloat()) }
                rewind()
            }.array()
        ).map { toDegrees(it.toDouble()) }
    }
}
