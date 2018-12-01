/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef

import arrow.core.Either
import com.google.common.collect.ImmutableMap
import com.neuronrobotics.kinematicschef.classifier.ChainIdentifier
import com.neuronrobotics.kinematicschef.classifier.ClassifierError
import com.neuronrobotics.kinematicschef.classifier.DhClassifier
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngle
import com.neuronrobotics.kinematicschef.util.toImmutableMap
import com.neuronrobotics.kinematicschef.util.toSimpleMatrix
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import javax.inject.Inject

/**
 * A [DhInverseSolver] which attempts to generate and cache an analytic solver by deriving the
 * chain's Euler angles. If an analytic solver cannot be generated, an iterative solver will be
 * used instead.
 */
class InverseKinematicsEngine
@Inject internal constructor(
    private val chainIdentifier: ChainIdentifier,
    private val dhClassifier: DhClassifier
) : DhInverseSolver {

    /**
     * Calculate the joint angles for the system.
     *
     * @param target The target frame transformation.
     * @param jointSpaceVector The current joint angles.
     * @param chain The DH params for the system.
     * @return The joint angles necessary to meet the target.
     */
    override fun inverseKinematics(
        target: TransformNR,
        jointSpaceVector: DoubleArray,
        chain: DHChain
    ): DoubleArray {
        val dhParams = chain.toDhParams()
        val targetMatrix = target.toSimpleMatrix()
        val chainElements = chainIdentifier.identifyChain(dhParams)
        val newJointAngles = Array(jointSpaceVector.size) { 0.0 }

        val eulerAngles = chainElements
            .mapNotNull { it as? SphericalWrist }
            .map {
                it to dhClassifier.deriveEulerAngles(it)
            }
            .toImmutableMap()

        validateEulerAngles(eulerAngles)

        val wrist = chainElements.last() as? SphericalWrist
            ?: return CalikoInverseKinematicsEngine().inverseKinematics(
                target,
                jointSpaceVector,
                chain
            )

        val wristCenter = wrist.center(target.toSimpleMatrix())

        when (dhParams.first().r) {
            0.0 -> {
                // next joint is along Z axis of shoulder

                // check for singularity, if so then the shoulder joint angle does not need to change
                if (targetMatrix[0, 3] == 0.0 && targetMatrix[1, 3] == 0.0) {
                    newJointAngles[0] = jointSpaceVector[0]
                } else {
                    val theta1 = Math.toDegrees(Math.atan2(targetMatrix[0, 3], targetMatrix[1, 3]))
                    val theta2 = Math.toDegrees(Math.PI + theta1)
                }
            }
            else -> {
                // left/right arm configuration
            }
        }

        return CalikoInverseKinematicsEngine().inverseKinematics(target, jointSpaceVector, chain)
    }

    /**
     * Throw an exception if there was an error while deriving the euler angles.
     */
    private fun validateEulerAngles(
        eulerAngles: ImmutableMap<SphericalWrist, Either<ClassifierError, EulerAngle>>
    ) {
        eulerAngles
            .filterValues { it.isLeft() }
            .values
            .mapNotNull { elem ->
                elem.fold(
                    {
                        it.errorString
                    },
                    {
                        null
                    }
                )
            }
            .fold("") { acc, elem ->
                """
                    $acc
                    $elem
                """.trimIndent()
            }.let {
                if (it.isNotEmpty()) {
                    throw UnsupportedOperationException(it)
                }
            }
    }
}
