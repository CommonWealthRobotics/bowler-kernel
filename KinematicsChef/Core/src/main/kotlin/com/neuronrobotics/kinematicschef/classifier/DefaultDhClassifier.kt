/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
@file:Suppress("FunctionName")

package com.neuronrobotics.kinematicschef.classifier

import arrow.core.Either
import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngle
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleXYX
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleXYZ
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleXZX
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleXZY
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleYXY
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleYXZ
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleYZX
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleYZY
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleZXY
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleZXZ
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleZYX
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleZYZ
import com.neuronrobotics.kinematicschef.util.asPointMatrix
import com.neuronrobotics.kinematicschef.util.getTranslation
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.immutableMapOf
import org.ejml.simple.SimpleMatrix

internal class DefaultDhClassifier
internal constructor() : DhClassifier {

    /**
     * Determine the Euler angles for a [SphericalWrist].
     *
     * @param wrist The wrist to classify.
     * @param priorChain The links in the chain before the [wrist].
     * @param tipTransform The frame transformation for the tip of the chain.
     * @return The Euler angles or an error.
     */
    override fun deriveEulerAngles(
        wrist: SphericalWrist,
        priorChain: ImmutableList<DhParam>,
        tipTransform: SimpleMatrix
    ): Either<ClassifierError, EulerAngle> {
        val center = wrist.centerHomed(priorChain).asPointMatrix()
        val centerTransformed = center.mult(tipTransform.invert())
        val centerPosition = centerTransformed.getTranslation()

        return if (centerPosition[1] == 0.0 && centerPosition[2] == 0.0) {
            // Wrist lies on the x-axis, therefore its dh params are valid
            deriveEulerAngles(wrist)
        } else {
            Either.left(
                ClassifierError(
                    """
                    The wrist does not have valid DH Parameters. Transformed center position:
                    $centerPosition
                    """.trimIndent()
                )
            )
        }
    }

    /**
     * Determine the Euler angles for a [SphericalWrist].
     *
     * @param wrist The wrist to classify.
     * @return The Euler angles or an error.
     */
    fun deriveEulerAngles(
        wrist: SphericalWrist
    ): Either<ClassifierError, EulerAngle> {
        fun validateAlpha(alpha: Double) = alpha == 0.0 || alpha == 90.0 || alpha == -90.0
        fun validateTheta(theta: Double) = theta == 0.0 || theta == 90.0 || theta == -90.0

        val invalidParams = wrist.params.filter {
            !(validateAlpha(it.alpha) && validateTheta(it.theta))
        }

        if (invalidParams.isNotEmpty()) {
            return Either.left(
                ClassifierError(
                    """
                    The following DhParams are invalid:
                    ${invalidParams.joinToString("\n")}
                    """.trimIndent()
                )
            )
        }

        return deriveEulerAngles(wrist.params[0], wrist.params[1], wrist.params[2])
    }

    private fun deriveEulerAngles(
        link1: DhParam,
        link2: DhParam,
        link3: DhParam
    ): Either<ClassifierError, EulerAngle> {
        val eulerAngleMap = immutableMapOf(
            immutableListOf(0, 90, -90, 90, 0, -90) to EulerAngleZXZ,
            immutableListOf(0, -90, 90, 0, 0, 0) to EulerAngleZYZ,
            immutableListOf(0, 90, -90, 90, -90, -90) to EulerAngleZXY,
            immutableListOf(0, -90, 90, 0, 90, 90) to EulerAngleZYX,
            immutableListOf(-90, 90, -90, 90, 0, -90) to EulerAngleYXY,
            immutableListOf(-90, 90, -90, 0, 0, 0) to EulerAngleYZY,
            immutableListOf(-90, 90, -90, 90, 90, -90) to EulerAngleYXZ,
            immutableListOf(-90, 90, 90, 0, 90, 0) to EulerAngleYZX,
            immutableListOf(90, -90, 90, -90, 0, 90) to EulerAngleXYX,
            immutableListOf(90, -90, 90, 0, 0, 0) to EulerAngleXZX,
            immutableListOf(90, -90, 90, -90, -90, 0) to EulerAngleXYZ,
            immutableListOf(90, -90, -90, 0, -90, 0) to EulerAngleXZY
        )

        val wristInListFormat = immutableListOf(
            link1.alpha.toInt(), link2.alpha.toInt(), link3.alpha.toInt(),
            link1.theta.toInt(), link2.theta.toInt(), link3.theta.toInt()
        )

        return eulerAngleMap[wristInListFormat].let {
            if (it != null) {
                Either.right(it)
            } else {
                Either.left(failDerivation(link1, link2, link3))
            }
        }
    }

    private fun failDerivation(vararg params: DhParam): ClassifierError {
        return ClassifierError(
            """
            The wrist does not have Euler angles:
            ${params.joinToString(separator = "\n")}
            """.trimIndent()
        )
    }
}
