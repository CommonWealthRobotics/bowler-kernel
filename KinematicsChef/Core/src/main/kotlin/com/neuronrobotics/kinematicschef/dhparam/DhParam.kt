/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.dhparam

import com.neuronrobotics.kinematicschef.util.getFrameTranslationMatrix
import com.neuronrobotics.kinematicschef.util.getTranslation
import com.neuronrobotics.kinematicschef.util.identityFrameTransform
import com.neuronrobotics.kinematicschef.util.length
import com.neuronrobotics.kinematicschef.util.toImmutableList
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DHLink
import org.ejml.simple.SimpleMatrix
import java.lang.Math.toDegrees
import kotlin.math.abs
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
    val frameTransformation by lazy {
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
        }
    }

    /**
     * The translational length.
     */
    val length by lazy {
        val pointBeforeTransform = getFrameTranslationMatrix(0, 0, 0)
        val pointAfterTransform = pointBeforeTransform.mult(frameTransformation)

        pointAfterTransform.getTranslation()
            .minus(pointBeforeTransform.getTranslation())
            .length()
    }

    /**
     * The angle the translation points in.
     */
    val angle by lazy {
        frameTransformation.getTranslation().let {
            toDegrees(atan2(it[1], it[0]))
        }
    }

    fun toDHLink() = DHLink(d, theta, r, alpha)

    companion object {
        val zero = DhParam(0, 0, 0, 0)
    }
}

/**
 * Maps this [Collection] of [DhParam] into a frame transformation representing the transform of
 * the tip.
 */
fun Collection<DhParam>.toFrameTransformation() =
    fold(identityFrameTransform()) { acc, dhParam ->
        acc.mult(dhParam.frameTransformation)
    }

/**
 * Maps the [DHLink] into a [DhParam]. If the [DHLink] appears to have both [DHLink.theta] and
 * [DHLink.alpha] specified in radians, they will be converted to degrees.
 */
fun DHLink.toDhParam(): DhParam {
    val epsilon = 1e-14

    // Check for PI, PI/2, and 0
    fun checkValueIsRadians(value: Double) =
        abs(abs(value) - Math.PI) < epsilon || abs(abs(value) - Math.PI / 2) < epsilon || abs(value) < epsilon

    return if (checkValueIsRadians(theta) && checkValueIsRadians(alpha)) {
        DhParam(d, toDegrees(theta), r, toDegrees(alpha))
    } else {
        DhParam(d, theta, r, alpha)
    }
}

fun Collection<DHLink>.toDhParams() = map { it.toDhParam() }.toImmutableList()

fun DHChain.toDhParams() = links.toImmutableList().toDhParams()

fun Collection<DhParam>.toDHLinks() = map { it.toDHLink() }.toImmutableList()
