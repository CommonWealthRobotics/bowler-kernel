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
import com.neuronrobotics.kinematicschef.dhparam.RevoluteJoint
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.dhparam.toDhParamList
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.dhparam.toFrameTransformation
import com.neuronrobotics.kinematicschef.util.getTranslation
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.modulus
import com.neuronrobotics.kinematicschef.util.projectionOntoPlane
import com.neuronrobotics.kinematicschef.util.projectionOntoVector
import com.neuronrobotics.kinematicschef.util.toSimpleMatrix
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.ejml.simple.SimpleMatrix
import org.jlleitschuh.guice.key
import org.jlleitschuh.guice.module
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import javax.inject.Inject
import kotlin.math.PI
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
    ): DoubleArray = inverseKinematics(target.toSimpleMatrix(), jointSpaceVector, chain)

    fun inverseKinematics(
        target: SimpleMatrix,
        jointSpaceVector: DoubleArray,
        chain: DHChain
    ): DoubleArray {
        target.print()
        val dhParams = chain.toDhParams()
        val chainElements = immutableListOf(
            RevoluteJoint(immutableListOf(dhParams[0])),
            RevoluteJoint(immutableListOf(dhParams[1])),
            RevoluteJoint(immutableListOf(dhParams[2])),
            SphericalWrist(immutableListOf(dhParams[3], dhParams[4], dhParams[5]))
        )
//        val chainElements = chainIdentifier.identifyChain(dhParams)
//
//        val eulerAngles = chainElements
//            .mapNotNull { it as? SphericalWrist }
//            .map {
//                it to dhClassifier.deriveEulerAngles(
//                    it,
//                    chainElements.subList(0, chainElements.indexOf(it)),
//                    chainElements.subList(chainElements.indexOf(it) + 1, chainElements.size)
//                )
//            }.toImmutableMap()
//
//        // If there were any problems while deriving the spherical wrists' Euler angles we can't
//        // solve the chain analytically
//        if (eulerAngles.filter { it.value.isLeft() }.isNotEmpty()) {
//            useIterativeSolver()
//        }

        val wrist = chainElements.last() as? SphericalWrist ?: useIterativeSolver()

        val wristCenter = wrist.center(target)
        println("wristCenter: $wristCenter")
        val newJointAngles = DoubleArray(jointSpaceVector.size) { 0.0 }

        val dOffset = abs(
            wrist.centerHomed(
                chainElements.subList(0, chainElements.indexOf(wrist) + 1).toDhParamList()
            ).projectionOntoPlane(
                SimpleMatrix(3, 1).apply {
                    this[2, 0] = 1.0
                }
            ).extractMatrix(
                0, 2,
                0, 1
            ).projectionOntoVector(
                // TODO: Should this be along alpha or theta?
                // In the context of the cmm arm, alpha is the y component and theta is the x component
                SimpleMatrix(2, 1).apply {
                    this[0, 0] = cos(toRadians(dhParams[0].alpha))
                    this[1, 0] = sin(toRadians(dhParams[0].alpha))
                }
            ))
        println("dOffset: $dOffset")

        val lengthToWristSquared = wristCenter[0].pow(2) + wristCenter[1].pow(2) - dOffset.pow(2)
        println("lengthToWristSquared: $lengthToWristSquared")

        val heightOfFirstElbow = dhParams.subList(0, 2).toFrameTransformation().getTranslation()[2]

        when (dOffset) {
            0.0 -> {
                // next joint is along Z axis of shoulder
                // check for singularity, if so then the shoulder joint angle does not need to change
                if (wristCenter[0] == 0.0 && wristCenter[1] == 0.0) {
                    newJointAngles[0] = jointSpaceVector[0]
                } else {
                    // Normal atan2, spong writes his atan2 as (x, y) for some reason
                    val theta1SolutionA = atan2(wristCenter[1], wristCenter[0])
                    val theta1SolutionB = PI + theta1SolutionA

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
//                // left/right arm configuration
//                val phi = atan2(wristCenter[1], wristCenter[0])
////                val d = dhParams[0].r + dhParams[1].d + dhParams[2].d
//                val length = sqrt(lengthToWristSquared)
//
//                val theta1Left = phi - atan2(dOffset, length)
//                val theta1Right = phi + atan2(-1 * dOffset, -1 * length)
//
//                when {
//                    chain.jointAngleInBounds(theta1Left, 0) -> newJointAngles[0] = theta1Left
//                    chain.jointAngleInBounds(theta1Right, 0) -> newJointAngles[0] = theta1Right
//                    else -> useIterativeSolver()
//                }

                // Angle of shoulder
                val angleFromFirstLinkToWristCoR =
                    wrist.centerHomed(dhParams.subList(0, 3)).let {
                        atan2(it[1], it[0])
                    }

                newJointAngles[0] =
                    atan2(wristCenter[1], wristCenter[0]) - angleFromFirstLinkToWristCoR + PI
            }
        }

        // TODO: Implement offset addition when joints 2 and/or 3 have a non-zero d value (DH param)
        // compute theta3, then theta 2

        // spong 4.29 (xc^2 + yc^2 - d^2 + zc^2 - a2^2 - a3^2)/(2(a2)(a3))
        val a2 = dhParams[1].r
        val a3 = dhParams[2].r
        val elbowTarget = dhParams.subList(0, 3).toFrameTransformation().getTranslation() -
            dhParams.subList(0, 1).toFrameTransformation().getTranslation()
        val r = elbowTarget[0]
        val s = elbowTarget[2]

        val adjustedWristHeight = wristCenter[2] - heightOfFirstElbow
        val cosTheta3 = (r.pow(2) + s.pow(2) - a2.pow(2) - a3.pow(2)) / (2 * a2 * a3)

        val theta3ElbowUp = atan2(-sqrt(1 - cosTheta3.pow(2)), cosTheta3)
        val theta3ElbowDown = atan2(sqrt(1 - cosTheta3.pow(2)), cosTheta3)

        val theta2ElbowUp = atan2(s, r) -
            atan2(
                a3 * sin(theta3ElbowUp),
                a2 + a3 * cos(theta3ElbowUp)
            )

        val theta2ElbowDown = atan2(s, r) -
            atan2(
                a3 * sin(theta3ElbowDown),
                a2 + a3 * cos(theta3ElbowDown)
            )

//        // select elbow up or down based on smallest valid delta in theta2
//        when {
//            chain.jointAngleInBounds(theta2ElbowDown, 1)
//                && chain.jointAngleInBounds(theta2ElbowUp, 1) -> {
//                val comparison = abs(jointSpaceVector[1] - theta2ElbowDown)
//                    .compareTo(abs(jointSpaceVector[1] - theta2ElbowUp))
//
//                newJointAngles[1] = when {
//                    comparison > 0 -> theta2ElbowUp.also { newJointAngles[2] = theta3ElbowUp }
//                    else -> theta2ElbowDown.also { newJointAngles[2] = theta3ElbowDown }
//                }
//            }
//
//            chain.jointAngleInBounds(theta2ElbowDown, 1) -> {
//                newJointAngles[1] = theta2ElbowDown
//                newJointAngles[2] = theta3ElbowDown
//            }
//
//            chain.jointAngleInBounds(theta2ElbowUp, 1) -> {
//                newJointAngles[1] = theta2ElbowUp
//                newJointAngles[2] = theta3ElbowUp
//            }
//
//            else -> useIterativeSolver()
//        }
//
//        if (!chain.jointAngleInBounds(newJointAngles[2], 2)) {
//            useIterativeSolver()
//        }

        // TODO: Always pick elbow up for now
        newJointAngles[1] = theta2ElbowUp
        newJointAngles[2] = theta3ElbowUp

        // TODO: Implement solver for computing wrist joint angles
        // using previous angles for now
        newJointAngles[3] = jointSpaceVector[3]
        newJointAngles[4] = jointSpaceVector[4]
        newJointAngles[5] = jointSpaceVector[5]

        return newJointAngles.mapIndexed { index, elem ->
            when {
//                index == 0 ||
                index > 2 -> toDegrees(elem)
                else -> toDegrees(elem) + dhParams[index].theta
            }
        }.map {
            if (it > 360 || it < -360)
                it.modulus(360)
            else
                it
        }.toDoubleArray().also {
            println("jointAngles: ${it.joinToString()}")
        }
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
        // val link = factory.getLink(factory.linkConfigurations[index])
        // return jointAngle <= link.maxEngineeringUnits && jointAngle >= link.minEngineeringUnits
        // TODO: Convert jointAngle to degrees and use the real bounds
        return true
    }

    private fun useIterativeSolver(): Nothing = TODO("The chain must be solved iteratively.")

    companion object {
        internal fun inverseKinematicsEngineModule() = module {
            bind<ChainIdentifier>().to<DefaultChainIdentifier>()
            bind<DhClassifier>().to<DefaultDhClassifier>()
            bind<WristIdentifier>().to<DefaultWristIdentifier>()
        }

        /**
         * Get an instance of the [InverseKinematicsEngine].
         */
        @JvmStatic
        fun getInstance(): InverseKinematicsEngine {
            return Guice.createInjector(inverseKinematicsEngineModule())
                .getInstance(key<InverseKinematicsEngine>())
        }
    }
}
