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
import com.google.inject.Guice
import com.neuronrobotics.kinematicschef.classifier.ChainIdentifier
import com.neuronrobotics.kinematicschef.classifier.ClassifierError
import com.neuronrobotics.kinematicschef.classifier.DefaultChainIdentifier
import com.neuronrobotics.kinematicschef.classifier.DefaultDhClassifier
import com.neuronrobotics.kinematicschef.classifier.DefaultWristIdentifier
import com.neuronrobotics.kinematicschef.classifier.DhClassifier
import com.neuronrobotics.kinematicschef.classifier.WristIdentifier
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngle
import com.neuronrobotics.kinematicschef.util.toImmutableMap
import com.neuronrobotics.kinematicschef.util.toSimpleMatrix
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.jlleitschuh.guice.key
import org.jlleitschuh.guice.module
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
        val newJointAngles = DoubleArray(jointSpaceVector.size) { 0.0 }

        when (dhParams.first().r) {
            0.0 -> {
                // next joint is along Z axis of shoulder

                // check for singularity, if so then the shoulder joint angle does not need to change
                if (targetMatrix[0, 3] == 0.0 && targetMatrix[1, 3] == 0.0) {
                    newJointAngles[0] = jointSpaceVector[0]
                } else {
                    val theta1SolutionA = Math.toDegrees(Math.atan2(targetMatrix[0, 3], targetMatrix[1, 3]))
                    val theta1SolutionB = Math.toDegrees(Math.PI) + theta1SolutionA

                    when {
                        chain.jointAngleInBounds(theta1SolutionA, 0)
                                && chain.jointAngleInBounds(theta1SolutionB, 0) -> {
                            val comparison = Math.abs(jointSpaceVector[0] - theta1SolutionA)
                                    .compareTo(Math.abs(jointSpaceVector[0] - theta1SolutionB))

                            newJointAngles[0] = when {
                                comparison > 0 -> theta1SolutionB
                                else -> theta1SolutionA
                            }
                        }

                        chain.jointAngleInBounds(theta1SolutionA, 0) -> newJointAngles[0] = theta1SolutionA

                        chain.jointAngleInBounds(theta1SolutionB, 0) -> newJointAngles[0] = theta1SolutionB

                        else -> return CalikoInverseKinematicsEngine().inverseKinematics(
                                target,
                                jointSpaceVector,
                                chain
                        )
                    }
                }
            }
            else -> {
                // left/right arm configuration
                val phi = Math.atan2(targetMatrix[0, 3], targetMatrix[1, 3])
                val theta1Left = Math.toDegrees(phi
                        - Math.atan2(Math.sqrt(targetMatrix[0, 3] * targetMatrix[0, 3]
                        + targetMatrix[1, 3] * targetMatrix[1, 3] - dhParams.first().r * dhParams.first().r),
                        dhParams.first().r
                )
                )

                val theta1Right = Math.toDegrees(phi
                        + Math.atan2(-Math.sqrt(targetMatrix[0, 3] * targetMatrix[0, 3]
                        + targetMatrix[1, 3] * targetMatrix[1, 3] - dhParams.first().r * dhParams.first().r),
                        dhParams.first().r * -1
                )
                )

                //TODO: Pick between the left and right arm solutions
                //Using just left arm solution for now.

                if (chain.jointAngleInBounds(theta1Left, 0)) jointSpaceVector[0] = theta1Left
                else return CalikoInverseKinematicsEngine().inverseKinematics(
                        target,
                        jointSpaceVector,
                        chain
                )
            }
        }

        //TODO: Implement offset addition when joints 2 and/or 3 have a non-zero d value (DH param)
        //compute theta3, then theta 2

        //(xc^2 + yc^2 - d^2 + zc^2 - a2^2 - a3^2)/(2(a2)(a3))
        val cosTheta3 = (targetMatrix[0, 3] * targetMatrix[0, 3]
                + targetMatrix[1, 3] * targetMatrix[1, 3]
                - dhParams.first().r * dhParams.first().r
                + targetMatrix[2, 3] * targetMatrix[2, 3]
                - dhParams[1].r * dhParams[1].r
                - dhParams[2].r * dhParams[2].r) / (
                2.0 * dhParams[1].r * dhParams[2].r
                )

        val theta3ElbowUp = Math.atan2(cosTheta3, Math.sqrt(1 - cosTheta3 * cosTheta3))
        val theta3ElbowDown = Math.atan2(cosTheta3, -Math.sqrt(1 - cosTheta3 * cosTheta3))

        val theta2ElbowUp = Math.atan2(Math.sqrt(targetMatrix[0, 3] * targetMatrix[0, 3]
                + targetMatrix[1, 3] * targetMatrix[1, 3]
                - dhParams.first().r * dhParams.first().r),
                targetMatrix[2, 3]
        ) - Math.atan2(dhParams[1].r + dhParams[2].r * Math.cos(theta3ElbowUp),
                dhParams[2].r * Math.sin(theta3ElbowUp)
        )

        val theta2ElbowDown = Math.atan2(Math.sqrt(targetMatrix[0, 3] * targetMatrix[0, 3]
                + targetMatrix[1, 3] * targetMatrix[1, 3]
                - dhParams.first().r * dhParams.first().r),
                targetMatrix[2, 3]
        ) - Math.atan2(dhParams[1].r + dhParams[2].r * Math.cos(theta3ElbowUp),
                dhParams[2].r * Math.sin(theta3ElbowUp)
        )

        //select elbow up or down based on smallest valid delta in theta2
        when {
            chain.jointAngleInBounds(theta2ElbowDown, 1)
                    && chain.jointAngleInBounds(theta2ElbowUp, 1) -> {
                val comparison = Math.abs(jointSpaceVector[1] - theta2ElbowDown)
                        .compareTo(Math.abs(jointSpaceVector[1] - theta2ElbowUp))

                newJointAngles[1] = when {
                    comparison > 0 -> theta2ElbowUp.also { newJointAngles[2] = theta3ElbowUp }
                    else -> theta2ElbowDown.also { newJointAngles[2] = theta3ElbowDown }
                }
            }

            chain.jointAngleInBounds(theta2ElbowDown, 1) -> {
                newJointAngles[1] = theta2ElbowDown
                newJointAngles[2] = theta3ElbowDown
            }

            chain.jointAngleInBounds(theta2ElbowUp, 1) -> {
                newJointAngles[1] = theta2ElbowUp
                newJointAngles[2] = theta3ElbowUp
            }

            else -> return CalikoInverseKinematicsEngine().inverseKinematics(
                    target,
                    jointSpaceVector,
                    chain
            )
        }

        if (!chain.jointAngleInBounds(newJointAngles[2], 2)) {
            return CalikoInverseKinematicsEngine().inverseKinematics(
                target,
                jointSpaceVector,
                chain
            )
        }

        //TODO: Implement solver for computing wrist joint angles
        //using previous angles for now
        newJointAngles[3] = jointSpaceVector[3]
        newJointAngles[4] = jointSpaceVector[4]
        newJointAngles[5] = jointSpaceVector[5]

        return jointSpaceVector
    }

    companion object {
        internal fun inverseKinematicsEngineModule() = module {
            bind<ChainIdentifier>().to<DefaultChainIdentifier>()
            bind<DhClassifier>().to<DefaultDhClassifier>()
            bind<WristIdentifier>().to<DefaultWristIdentifier>()
        }

        fun getInstance(): InverseKinematicsEngine {
            return Guice.createInjector(inverseKinematicsEngineModule())
                    .getInstance(key<InverseKinematicsEngine>())
        }
    }

    /**
     * Throw an exception if there was an error while deriving the euler angles.
     */
    private fun validateEulerAngles(
            eulerAngles: ImmutableMap<SphericalWrist, Either<ClassifierError, EulerAngle>>
    ) {
        eulerAngles
                .values
                .mapNotNull { elem ->
                    elem.fold({ it.errorString }, { null })
                }
                .fold("") { acc, elem ->
                    """
                    |$acc
                    |$elem
                """.trimMargin().trimStart() // Trim the start to remove the initial newline
                }.let {
                    if (it.isNotEmpty()) {
                        throw UnsupportedOperationException(it)
                    }
                }
    }

    /**
     * Checks to see if a given joint angle is within the user-specified range of motion.
     *
     * @param jointAngle the joint angle to check against
     * @param index the index of the joint in the DH chain
     *
     * @return A [Boolean] indicating whether or not the given joint angle is within the valid range of motion.
     */
    private fun DHChain.jointAngleInBounds(jointAngle: Double, index: Int): Boolean {
        return jointAngle <= this.upperLimits[index] && jointAngle >= this.getlowerLimits()[index]
    }
}
