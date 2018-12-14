/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef

import com.google.common.collect.ImmutableList
import com.google.inject.Guice
import com.neuronrobotics.kinematicschef.classifier.ChainIdentifier
import com.neuronrobotics.kinematicschef.classifier.DefaultChainIdentifier
import com.neuronrobotics.kinematicschef.classifier.DefaultDhClassifier
import com.neuronrobotics.kinematicschef.classifier.DefaultWristIdentifier
import com.neuronrobotics.kinematicschef.classifier.DhClassifier
import com.neuronrobotics.kinematicschef.classifier.WristIdentifier
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.RevoluteJoint
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.dhparam.toDhParamList
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.dhparam.toFrameTransformation
import com.neuronrobotics.kinematicschef.util.getTranslation
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.length
import com.neuronrobotics.kinematicschef.util.modulus
import com.neuronrobotics.kinematicschef.util.projectionOntoPlane
import com.neuronrobotics.kinematicschef.util.projectionOntoVector
import com.neuronrobotics.kinematicschef.util.toImmutableList
import com.neuronrobotics.kinematicschef.util.toSimpleMatrix
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.apache.commons.math3.geometry.euclidean.threed.Rotation
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
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

        val dOffset = wrist.centerHomed(
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
        )

        val absDOffset = abs(dOffset)

        println("dOffset: $absDOffset")

        val lengthToWristSquared = wristCenter[0].pow(2) + wristCenter[1].pow(2) - absDOffset.pow(2)
        println("lengthToWristSquared: $lengthToWristSquared")

        val heightOfFirstElbow = dhParams.subList(0, 2).toFrameTransformation().getTranslation()[2]

        when (absDOffset) {
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
                    (atan2(wristCenter[1], wristCenter[0]) - angleFromFirstLinkToWristCoR + PI).modulus(2.0 * PI)
            }
        }

        // TODO: Implement offset addition when joints 2 and/or 3 have a non-zero d value (DH param)
        // compute theta3, then theta 2

        // spong 4.29 (xc^2 + yc^2 - d^2 + zc^2 - a2^2 - a3^2)/(2(a2)(a3))
        val a2 = dhParams[1].r
        val elbow2ToWristCenter = dhParams.subList(0, 5).toFrameTransformation().getTranslation() -
            dhParams.subList(0, 3).toFrameTransformation().getTranslation()
        elbow2ToWristCenter[1] = 0.0
        elbow2ToWristCenter[0] = 24.0
        val a3 = elbow2ToWristCenter.length()
        val elbow2ThetaOffset = atan2(elbow2ToWristCenter[2], elbow2ToWristCenter[0])

        val shoulderParam = DhParam(dhParams[0].d, toDegrees(newJointAngles[0]), dhParams[0].r, dhParams[0].alpha)

        val elbowTarget = wristCenter - shoulderParam.frameTransformation.getTranslation()
        val s = elbowTarget[2]

        elbowTarget[0] -= 14.0 * cos(newJointAngles[0] + PI * 0.5)
        elbowTarget[1] -= 14.0 * sin(newJointAngles[0] + PI * 0.5)
        elbowTarget[2] = 0.0

        val r = elbowTarget.length()

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

        // TODO: Always pick elbow up for now
        newJointAngles[1] = theta2ElbowUp
        newJointAngles[2] = theta3ElbowUp - elbow2ThetaOffset

        // TODO: Implement solver for computing wrist joint angles
        // using previous angles for now
        val wristOrigin = dhParams.subList(0, 3).forwardKinematics(newJointAngles).getTranslation()
        val wristCenterToTip = target.getTranslation() - wristCenter

        val u1 = Vector3D(wristOrigin[0], wristOrigin[1], wristOrigin[2])
        val u2 = Vector3D(wristCenter[0], wristCenter[1], wristCenter[2])
        val v1 = Vector3D(wristCenter[0], wristCenter[1], wristCenter[2])
        val v2 = Vector3D(target.getTranslation()[0], target.getTranslation()[1], target.getTranslation()[2])

        val rotation = Rotation(u1, u2, v1, v2)
        val angles = rotation.getAngles(RotationOrder.XYX)

        //TODO: move frame to first wrist joint and orient it so that the first wrist's joint axes align with xyz
        /* We also need to make sure the target (the tip of the wrist) gets rotated in respect to this new frame
         * in order to do the next theta calculations, else they break down when the wrist axes aren't already aligned.
         */

        //uncomment this once frame gets rotated
//        if (abs(xTip) < 0.001 && abs(yTip) < 0.001) {
//            newJointAngles[3] = jointSpaceVector[3]
//        } else {
//            newJointAngles[3] = atan2(yTip, xTip)
//        }
//
//        newJointAngles[4] = atan2(wristS, rTip) + PI * 0.5
//        //second solution for wrist center angle, basically 180 degree rotation
//        val jointAngles4Solution2 = atan2(wristS, rTip) - PI * 0.5

        newJointAngles[3] = angles[0]
        newJointAngles[4] = angles[1]
        newJointAngles[5] = angles[2]

//        newJointAngles[3] = jointSpaceVector[3]
//        newJointAngles[4] = jointSpaceVector[4]

        //TODO: tip rotation via the last joint, getting rest of wrist aligned will make this easy
//        newJointAngles[5] = jointSpaceVector[5]

        return newJointAngles.mapIndexed { index, elem ->
            when {
//                index == 0 ||
                index > 2 -> toDegrees(elem)
                else -> toDegrees(elem) + dhParams[index].theta
            }
        }.map {
            if (it >= 360 || it <= -360)
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

    private fun ImmutableList<DhParam>.forwardKinematics(thetas : DoubleArray) : SimpleMatrix {
        val paramList = ArrayList<DhParam>()

        for (i in 0 until this.size) {
            when (i) {
                0 -> paramList.add(DhParam(this[i].d, toDegrees(thetas[i]), this[i].r, this[i].alpha))
                else -> paramList.add(DhParam(this[i].d, -toDegrees(thetas[i]), this[i].r, this[i].alpha))
            }
        }

        return paramList.toImmutableList().toFrameTransformation()
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
