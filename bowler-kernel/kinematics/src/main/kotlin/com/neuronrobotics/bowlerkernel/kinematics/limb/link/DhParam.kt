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
package com.neuronrobotics.bowlerkernel.kinematics.limb.link

import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import org.apache.commons.math3.linear.MatrixUtils
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * A DH parameter. [theta] and [alpha] must be specified in degrees.
 */
data class DhParam(
    val d: Double,
    val theta: Double,
    val r: Double,
    val alpha: Double
) {

    constructor(d: Number, theta: Number, r: Number, alpha: Number) :
        this(d.toDouble(), theta.toDouble(), r.toDouble(), alpha.toDouble())

    /**
     * The 4x4 center of rotation frame transformation this [DhParam] represents. Computes using
     * the normal convention (CoR n to CoR n+1).
     */
    val frameTransformation: FrameTransformation by lazy {
        MatrixUtils.createRealIdentityMatrix(4).apply {
            val thetaRadians = Math.toRadians(theta)
            val alphaRadians = Math.toRadians(alpha)

            setEntry(0, 0, cos(thetaRadians))
            setEntry(1, 0, sin(thetaRadians))

            setEntry(0, 1, -sin(thetaRadians) * cos(alphaRadians))
            setEntry(1, 1, cos(thetaRadians) * cos(alphaRadians))
            setEntry(2, 1, sin(alphaRadians))

            setEntry(0, 2, sin(thetaRadians) * sin(alphaRadians))
            setEntry(1, 2, -cos(thetaRadians) * sin(alphaRadians))
            setEntry(2, 2, cos(alphaRadians))

            setEntry(0, 3, r * cos(thetaRadians))
            setEntry(1, 3, r * sin(thetaRadians))
            setEntry(2, 3, d)
        }.let { FrameTransformation.fromMatrix(it) }
    }

    /**
     * The translational length.
     */
    val length: Double = hypot(d, r)

    /**
     * The angle the translation points in.
     */
    val angle: Double = theta

    companion object {
        val zero = DhParam(0, 0, 0, 0)
    }
}

/**
 * Maps this [Collection] of [DhParam] into a frame transformation representing the transform of
 * the tip.
 */
fun Collection<DhParam>.toFrameTransformation(): FrameTransformation =
    fold(FrameTransformation.identity) { acc, dhParam ->
        acc * dhParam.frameTransformation
    }
