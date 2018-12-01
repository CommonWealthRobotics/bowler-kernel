/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.dhparam

import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.util.getPointMatrix
import com.neuronrobotics.kinematicschef.util.getTranslation
import com.neuronrobotics.kinematicschef.util.identityFrameTransform
import com.neuronrobotics.kinematicschef.util.length
import com.neuronrobotics.kinematicschef.util.toImmutableList
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DHLink
import org.ejml.simple.SimpleMatrix
import kotlin.math.cos
import kotlin.math.sin

/**
 * A DH parameter. [theta] and [alpha] must be specified in degrees.
 */
internal data class DhParam
internal constructor(
    val d: Double,
    val theta: Double,
    val r: Double,
    val alpha: Double
) {

    constructor(d: Number, theta: Number, r: Number, alpha: Number) :
        this(d.toDouble(), theta.toDouble(), r.toDouble(), alpha.toDouble())

    /**
     * Calculates the translational length of this [DhParam].
     */
    internal fun length(): Double {
        val pointBeforeTransform = getPointMatrix(0, 0, 0)
        val pointAfterTransform = pointBeforeTransform.mult(toFrameTransformation())

        return pointAfterTransform.getTranslation()
            .minus(pointBeforeTransform.getTranslation())
            .length()
    }

    internal fun toDHLink() = DHLink(d, theta, r, alpha)

    companion object {
        val zero = DhParam(0, 0, 0, 0)
    }
}

internal fun DHLink.toDhParam() = DhParam(d, theta, r, alpha)

internal fun ImmutableList<DHLink>.toDhParams() = map { it.toDhParam() }.toImmutableList()

internal fun DHChain.toDhParams() = links.toImmutableList().toDhParams()

/**
 * Maps this [DhParam] into a frame transformation. Expects [DhParam.theta] and [DhParam.alpha]
 * to be specified in degrees.
 */
internal fun DhParam.toFrameTransformation() =
    SimpleMatrix(4, 4).apply {
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
        this[3, 3] = 1.0
    }

/**
 * Maps this [Collection] of [DhParam] into a frame transformation representing the transform of
 * the tip.
 */
internal fun Collection<DhParam>.toFrameTransformation() =
    fold(identityFrameTransform()) { acc, dhParam ->
        acc.mult(dhParam.toFrameTransformation())
    }
