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
import org.octogonapus.ktguava.collections.toImmutableList
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Solves 3 DOF open chains.
 */
class ThreeDofSolver : InverseKinematicsSolver {

    @SuppressWarnings("LongMethod", "ComplexMethod")
    override fun solveChain(
        links: List<Link>,
        currentJointAngles: List<Double>,
        jointLimits: List<JointLimits>,
        targetFrameTransform: FrameTransformation
    ): List<Double> {
        require(links.size >= 3) {
            "Must have at least 3 links, given ${links.size}"
        }

        require(links.size == currentJointAngles.size) {
            """
            Links and joint angles must have equal length:
            Number of links: ${links.size}
            Number of joint angles: ${currentJointAngles.size}
            """.trimIndent()
        }

        require(links.size == jointLimits.size) {
            """
            Links and joint limits must have equal length:
            Number of links: ${links.size}
            Number of joint limits: ${jointLimits.size}
            """.trimIndent()
        }

        val params = links.map { it.dhParam }

        val lengthXYPlaneVec =
            hypot(targetFrameTransform.translationX, targetFrameTransform.translationY)

        // Handle zero by moving the target out along x a tiny bit
        if (lengthXYPlaneVec == 0.0) {
            return solveChain(
                links,
                currentJointAngles,
                jointLimits,
                targetFrameTransform * FrameTransformation.fromTranslation(1e-6, 0, 0)
            )
        }

        val angleXYPlaneVec = asin(targetFrameTransform.translationY / lengthXYPlaneVec)

        val d = params[1].d - params[2].d
        val r = params[0].r
        val angleRectangleAdjustedXY = asin(d / lengthXYPlaneVec)
        val lengthRectangleAdjustedXY = lengthXYPlaneVec * cos(angleRectangleAdjustedXY) - r

        val orientation = (angleXYPlaneVec - angleRectangleAdjustedXY).let {
            if (abs(toDegrees(it)) < 1e-6) 0.0 else it
        }.let {
            // Orientation is off by 180 for negative x values for some reason so offset it
            if (targetFrameTransform.translationX < 0) it + PI else it
        }

        val ySet = lengthRectangleAdjustedXY * sin(orientation)
        val xSet = lengthRectangleAdjustedXY * cos(orientation)
        val zSet = targetFrameTransform.translationZ - params[0].d
        val vec = sqrt(xSet.pow(2) + ySet.pow(2) + zSet.pow(2))
        val l1 = params[1].r
        val l2 = params[2].r

        if (vec > l1 + l2 || vec < 0 || lengthRectangleAdjustedXY < 0) {
            throw IllegalStateException(
                """
                Hypot too long:
                vec: $vec
                l1: $l1
                l2: $l2
                lengthRectangleAdjustedXY: $lengthRectangleAdjustedXY
                """.trimIndent()
            )
        }

        val inv = DoubleArray(links.size)
        inv[0] = toDegrees(orientation)

        val elevation = asin(zSet / vec)
        val (A, C) = lawOfTriangles(l2, l1, vec)
        inv[1] = -toDegrees(A + elevation + toRadians(params[1].theta))

        inv[2] = when (params[1].alpha.roundToInt()) {
            180 -> toDegrees(C) - 180 - params[2].theta
            0 -> -toDegrees(C) + params[2].theta
            else -> currentJointAngles[2]
        }

        return inv.toImmutableList()
    }

    private fun lawOfTriangles(a: Double, b: Double, c: Double): Pair<Double, Double> {
        val outA = acos((b.pow(2) + c.pow(2) - a.pow(2)) / (2 * b * c))
        val outB = acos((c.pow(2) + a.pow(2) - b.pow(2)) / (2 * a * c))
        val outC = PI - outA - outB
        return outA to outC
    }
}
