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
import com.neuronrobotics.bowlerkernel.kinematics.motion.length
import org.ejml.simple.SimpleMatrix
import kotlin.math.atan2
import kotlin.math.cos
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
        SimpleMatrix.identity(4).apply {
            val thetaRadians = Math.toRadians(theta)
            val alphaRadians = Math.toRadians(alpha)

            this[0, 0] = cos(thetaRadians)
            this[1, 0] = sin(thetaRadians)

            this[0, 1] = -sin(thetaRadians) * cos(alphaRadians)
            this[1, 1] = cos(thetaRadians) * cos(alphaRadians)
            this[2, 1] = sin(alphaRadians)

            this[0, 2] = sin(thetaRadians) * sin(alphaRadians)
            this[1, 2] = -cos(thetaRadians) * sin(alphaRadians)
            this[2, 2] = cos(alphaRadians)

            this[0, 3] = r * cos(thetaRadians)
            this[1, 3] = r * sin(thetaRadians)
            this[2, 3] = d
        }.let { FrameTransformation.fromSimpleMatrix(it) }
    }

    /**
     * The translational length.
     */
    val length: Double by lazy {
        val pointBeforeTransform = FrameTransformation.fromTranslation(0, 0, 0)
        val pointAfterTransform = pointBeforeTransform * frameTransformation
        (pointAfterTransform.getTranslation() - pointBeforeTransform.getTranslation()).length()
    }

    /**
     * The angle the translation points in.
     */
    val angle: Double by lazy {
        frameTransformation.getTranslation().let {
            Math.toDegrees(atan2(it[1], it[0]))
        }
    }

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
