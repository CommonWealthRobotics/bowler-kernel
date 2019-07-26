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

import Jama.Matrix
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
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
        Matrix.identity(4, 4).apply {
            val thetaRadians = Math.toRadians(theta)
            val alphaRadians = Math.toRadians(alpha)

            val cosTheta = cos(thetaRadians)
            val sinTheta = sin(thetaRadians)
            val cosAlpha = cos(alphaRadians)
            val sinAlpha = sin(alphaRadians)

            this[0, 0] = cosTheta
            this[1, 0] = sinTheta

            this[0, 1] = -sinTheta * cosAlpha
            this[1, 1] = cosTheta * cosAlpha
            this[2, 1] = sinAlpha

            this[0, 2] = sinTheta * sinAlpha
            this[1, 2] = -cosTheta * sinAlpha
            this[2, 2] = cosAlpha

            this[0, 3] = r * cosTheta
            this[1, 3] = r * sinTheta
            this[2, 3] = d
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
