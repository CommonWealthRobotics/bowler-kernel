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
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.immutableMapOf

internal class DefaultDhClassifier
internal constructor() : DhClassifier {

    /**
     * Determine the Euler angles for a [SphericalWrist].
     *
     * @param wrist The wrist to classify. The thetas must be specified as offsets.
     * @return The Euler angles or an error.
     */
    override fun deriveEulerAngles(
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
                        |The following DhParams are invalid:
                        |${invalidParams.joinToString("\n")}
                    """.trimMargin()
                )
            )
        }

        return deriveEulerAngles(wrist.params[0], wrist.params[1], wrist.params[2])
    }

    /**
     * This implementation would also technically work as a [WristIdentifier] but this requires
     * thetas to be specified as offsets so it is very fragile with respect to the actual value of
     * theta.
     */
    private fun deriveEulerAngles(
        link1: DhParam,
        link2: DhParam,
        link3: DhParam
    ): Either<ClassifierError, EulerAngle> {
        /**
         * Map the wrist into the [eulerAngleMap] format.
         */
        val wristInListFormat = immutableListOf(
            link1.alpha.toInt(), link2.alpha.toInt(), link3.alpha.toInt(),
            link1.theta.toInt(), link2.theta.toInt(), link3.theta.toInt()
        )

        // Use indexing into the map to pattern match the wrist
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
                |The wrist does not have Euler angles:
                |${params.joinToString("\n")}
            """.trimMargin()
        )
    }

    companion object {
        /**
         * This maps the alphas and thetas for the three links to their Euler angles.
         *
         * Format:
         * alpha 1, alpha 2, alpha 3, theta offset 1, theta offset 2, theta offset 3
         */
        private val eulerAngleMap = immutableMapOf(
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
    }
}
