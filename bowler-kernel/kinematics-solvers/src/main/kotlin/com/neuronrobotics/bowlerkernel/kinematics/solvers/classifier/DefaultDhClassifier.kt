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
@file:Suppress("FunctionName")

package com.neuronrobotics.bowlerkernel.kinematics.solvers.classifier

import arrow.core.Either
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.solvers.dhparam.DhChainElement
import com.neuronrobotics.bowlerkernel.kinematics.solvers.dhparam.SphericalWrist
import com.neuronrobotics.bowlerkernel.kinematics.solvers.eulerangle.EulerAngle
import com.neuronrobotics.bowlerkernel.kinematics.solvers.eulerangle.EulerAngleXYX
import com.neuronrobotics.bowlerkernel.kinematics.solvers.eulerangle.EulerAngleXYZ
import com.neuronrobotics.bowlerkernel.kinematics.solvers.eulerangle.EulerAngleXZX
import com.neuronrobotics.bowlerkernel.kinematics.solvers.eulerangle.EulerAngleXZY
import com.neuronrobotics.bowlerkernel.kinematics.solvers.eulerangle.EulerAngleYXY
import com.neuronrobotics.bowlerkernel.kinematics.solvers.eulerangle.EulerAngleYXZ
import com.neuronrobotics.bowlerkernel.kinematics.solvers.eulerangle.EulerAngleYZX
import com.neuronrobotics.bowlerkernel.kinematics.solvers.eulerangle.EulerAngleYZY
import com.neuronrobotics.bowlerkernel.kinematics.solvers.eulerangle.EulerAngleZXY
import com.neuronrobotics.bowlerkernel.kinematics.solvers.eulerangle.EulerAngleZXZ
import com.neuronrobotics.bowlerkernel.kinematics.solvers.eulerangle.EulerAngleZYX
import com.neuronrobotics.bowlerkernel.kinematics.solvers.eulerangle.EulerAngleZYZ
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableMapOf
import org.octogonapus.ktguava.collections.toImmutableList

/**
 * Derives Euler angles for a [SphericalWrist].
 */
class DefaultDhClassifier : DhClassifier {

    /**
     * Determine the Euler angles for a [SphericalWrist].
     *
     * @param wrist The wrist to classify. The thetas must be specified as offsets.
     * @return The Euler angles or an error.
     */
    override fun deriveEulerAngles(wrist: SphericalWrist): Either<String, EulerAngle> {
        fun validateAlpha(alpha: Double) = alpha == 0.0 || alpha == 90.0 || alpha == -90.0
        fun validateTheta(theta: Double) = theta == 0.0 || theta == 90.0 || theta == -90.0

        val invalidParams = wrist.params.filter {
            !(validateAlpha(it.alpha) && validateTheta(it.theta))
        }

        if (invalidParams.isNotEmpty()) {
            return failDerivation(invalidParams.toImmutableList())
        }

        return deriveEulerAngles(wrist.params[0], wrist.params[1], wrist.params[2])
    }

    /**
     * Determine the Euler angles for a [SphericalWrist].
     *
     * @param wrist The wrist to classify. The thetas must be specified as offsets.
     * @param priorChain The chain elements before the wrist.
     * @param followingChain The chain elements after the wrist.
     * @return The Euler angles or an error.
     */
    override fun deriveEulerAngles(
        wrist: SphericalWrist,
        priorChain: ImmutableList<DhChainElement>,
        followingChain: ImmutableList<DhChainElement>
    ): Either<String, EulerAngle> {
        return deriveEulerAngles(wrist).fold(
            {
                if (followingChain.isEmpty()) {
                    // If the wrist is the last in the chain, then the 3rd link alpha is effectively a free parameter
                    // So if the wrist matches except for the last alpha, then it still technically matches
                    val newAngles = listOf(-90, 0, 90).map { newAlpha ->
                        deriveEulerAngles(
                            SphericalWrist(
                                immutableListOf(
                                    wrist.params[0],
                                    wrist.params[1],
                                    wrist.params[2].copy(
                                        alpha = newAlpha.toDouble()
                                    )
                                )
                            )
                        )
                    }.mapNotNull { newEulerAngles ->
                        newEulerAngles.fold(
                            { null },
                            { it }
                        )
                    }

                    when {
                        newAngles.size == 1 -> Either.right(newAngles.first())
                        else -> failDerivation(wrist.params)
                    }
                } else {
                    // The wrist is not the last in the chain, so the 3rd link alpha is not a free parameter
                    failDerivation(wrist.params)
                }
            },
            {
                Either.right(it)
            }
        )
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
    ): Either<String, EulerAngle> {
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
                failDerivation(immutableListOf(link1, link2, link3))
            }
        }
    }

    /**
     * Fail because the wrist does not have Euler angles.
     */
    private fun failDerivation(params: ImmutableList<DhParam>) =
        Either.left(
            """
            |The wrist does not have Euler angles:
            |${params.joinToString("\n")}
            """.trimMargin()
        )

    companion object {
        /**
         * This maps the alphas and thetas for the three links to their Euler angles.
         * These values taken from "Denavit-Hartenberg Parameterization of Euler Angles" by S. V.
         * Shah, S. K. Saha, and J. K. Dutt.
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
