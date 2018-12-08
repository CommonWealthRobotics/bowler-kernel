/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef

import com.google.inject.Guice
import com.neuronrobotics.kinematicschef.classifier.ChainIdentifier
import com.neuronrobotics.kinematicschef.classifier.DefaultChainIdentifier
import com.neuronrobotics.kinematicschef.classifier.DefaultDhClassifier
import com.neuronrobotics.kinematicschef.classifier.DefaultWristIdentifier
import com.neuronrobotics.kinematicschef.classifier.DhClassifier
import com.neuronrobotics.kinematicschef.classifier.WristIdentifier
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.util.toImmutableMap
import com.neuronrobotics.kinematicschef.util.toSimpleMatrix
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.jlleitschuh.guice.key
import org.jlleitschuh.guice.module
import java.lang.Math.toDegrees
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

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
            .map { it to dhClassifier.deriveEulerAngles(it) }
            .toImmutableMap()

        // If there were any problems while deriving the spherical wrists' Euler angles we can't
        // solve the chain analytically
        if (eulerAngles.filter { it.value.isLeft() }.isNotEmpty()) {
            useIterativeSolver()
        }

        val wrist = chainElements.last() as? SphericalWrist ?: useIterativeSolver()

        val wristCenter = wrist.center(target.toSimpleMatrix())
        val newJointAngles = DoubleArray(jointSpaceVector.size) { 0.0 }

        // TODO: Jason please verify this variable name, idk what this length actually is
        val lengthToWrist = targetMatrix[0, 3].pow(2) + targetMatrix[1, 3].pow(2) - dhParams.first().r.pow(2)

        when (dhParams.first().r) {
            0.0 -> {
                // next joint is along Z axis of shoulder

                // check for singularity, if so then the shoulder joint angle does not need to change
                if (targetMatrix[0, 3] == 0.0 && targetMatrix[1, 3] == 0.0) {
                    newJointAngles[0] = jointSpaceVector[0]
                } else {
                    val theta1SolutionA = toDegrees(atan2(targetMatrix[0, 3], targetMatrix[1, 3]))
                    val theta1SolutionB = 180 + theta1SolutionA

                    when {
                        chain.jointAngleInBounds(theta1SolutionA, 0) &&
                            chain.jointAngleInBounds(theta1SolutionB, 0) -> {
                            // TODO: Simplify this to just do a simple comparison without calling compareTo()
                            val comparison = abs(jointSpaceVector[0] - theta1SolutionA)
                                .compareTo(abs(jointSpaceVector[0] - theta1SolutionB))

                            newJointAngles[0] = when {
                                comparison > 0 -> theta1SolutionB
                                else -> theta1SolutionA
                            }
                        }

                        chain.jointAngleInBounds(theta1SolutionA, 0) -> newJointAngles[0] =
                            theta1SolutionA

                        chain.jointAngleInBounds(theta1SolutionB, 0) -> newJointAngles[0] =
                            theta1SolutionB

                        else -> throw IllegalStateException("The chain must be solved iteratively.")
                    }
                }
            }

            else -> {
                // left/right arm configuration
                val phi = atan2(targetMatrix[0, 3], targetMatrix[1, 3])
                val length = sqrt(
                    lengthToWrist
                )

                val theta1Left = toDegrees(phi - atan2(length, dhParams.first().r))
                val theta1Right = toDegrees(phi + atan2(-1 * length, -1 * dhParams.first().r))

                // TODO: Pick between the left and right arm solutions
                // Using just left arm solution for now.
                if (chain.jointAngleInBounds(theta1Left, 0)) {
                    jointSpaceVector[0] = theta1Left
                } else {
                    useIterativeSolver()
                }
            }
        }

        // TODO: Implement offset addition when joints 2 and/or 3 have a non-zero d value (DH param)
        // compute theta3, then theta 2

        // spong 4.29 (xc^2 + yc^2 - d^2 + zc^2 - a2^2 - a3^2)/(2(a2)(a3))
        val cosTheta3 =
            (lengthToWrist + targetMatrix[2, 3].pow(2) - dhParams[1].r.pow(2) - dhParams[2].r.pow(2)) / (2.0 * dhParams[1].r * dhParams[2].r)

        val theta3ElbowUp = atan2(cosTheta3, sqrt(1 - cosTheta3.pow(2)))
        val theta3ElbowDown = atan2(cosTheta3, -1 * sqrt(1 - cosTheta3.pow(2)))

        val theta2ElbowUp = atan2(sqrt(lengthToWrist), targetMatrix[2, 3]) - atan2(
            dhParams[1].r + dhParams[2].r * cos(theta3ElbowUp), dhParams[2].r * sin(theta3ElbowUp)
        )
        val theta2ElbowDown = atan2(sqrt(lengthToWrist), targetMatrix[2, 3]) - atan2(
            dhParams[1].r + dhParams[2].r * cos(theta3ElbowDown), dhParams[2].r * sin(theta3ElbowDown)
        )

        // select elbow up or down based on smallest valid delta in theta2
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

            else -> useIterativeSolver()
        }

        if (!chain.jointAngleInBounds(newJointAngles[2], 2)) {
            useIterativeSolver()
        }

        // TODO: Implement solver for computing wrist joint angles
        // using previous angles for now
        newJointAngles[3] = jointSpaceVector[3]
        newJointAngles[4] = jointSpaceVector[4]
        newJointAngles[5] = jointSpaceVector[5]

        return jointSpaceVector
    }

    /**
     * Checks to see if a given joint angle is within the user-specified range of motion.
     *
     * @param jointAngle The joint angle to check against.
     * @param index The index of the joint in the DH chain.
     *
     * @return Whether or not the given joint angle is within the valid range of motion.
     */
    private fun DHChain.jointAngleInBounds(jointAngle: Double, index: Int): Boolean {
        val link = factory.getLink(factory.linkConfigurations[index])
        return jointAngle <= link.maxEngineeringUnits && jointAngle >= link.minEngineeringUnits
    }

    private fun useIterativeSolver(): Nothing = TODO("The chain must be solved iteratively.")

    companion object {
        internal fun inverseKinematicsEngineModule() = module {
            bind<ChainIdentifier>().to<DefaultChainIdentifier>()
            bind<DhClassifier>().to<DefaultDhClassifier>()
            bind<WristIdentifier>().to<DefaultWristIdentifier>()
        }

        @JvmStatic
        fun getInstance(): InverseKinematicsEngine {
            return Guice.createInjector(inverseKinematicsEngineModule())
                .getInstance(key<InverseKinematicsEngine>())
        }
    }
}
