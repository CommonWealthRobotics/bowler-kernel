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
import com.neuronrobotics.kinematicschef.util.toImmutableMap
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder
import javax.inject.Inject

/**
 * A [DhInverseSolver] which attempts to generate and cache  an analytic solver by deriving the
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
        val chainElements = chainIdentifier.identifyChain(chain.toDhParams())
        val eulerAngles = chainElements
            .mapNotNull { it as? SphericalWrist }
            .map { it to dhClassifier.deriveEulerAngles(it) }
            .toImmutableMap()

        validateEulerAngles(eulerAngles)

        // TODO: Write the analytic solver here
        TODO("not implemented")
    }

    /**
     * Throw an exception if there was an error while deriving the euler angles.
     */
    private fun validateEulerAngles(
        eulerAngles: ImmutableMap<SphericalWrist, Either<ClassifierError, RotationOrder>>
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
