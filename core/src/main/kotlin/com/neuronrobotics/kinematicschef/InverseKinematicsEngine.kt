/*
 * This file is part of kinematics-chef.
 *
 * kinematics-chef is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kinematics-chef is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with kinematics-chef.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.kinematicschef

import com.google.common.collect.ImmutableList
import com.google.inject.Guice
import com.neuronrobotics.kinematicschef.classifier.*
import com.neuronrobotics.kinematicschef.dhparam.*
import com.neuronrobotics.kinematicschef.util.*
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
import javax.inject.Inject
import kotlin.math.*

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
        val dhParams = chain.toDhParams()
        val chainElements = immutableListOf(
            RevoluteJoint(immutableListOf(dhParams[0])),
            RevoluteJoint(immutableListOf(dhParams[1])),
            RevoluteJoint(immutableListOf(dhParams[2])),
            SphericalWrist(immutableListOf(dhParams[3], dhParams[4], dhParams[5]))
        )

        val wrist = chainElements.last() as? SphericalWrist ?: useIterativeSolver()

        val wristCenter = wrist.center(target)
        val newJointAngles = DoubleArray(jointSpaceVector.size) { 0.0 }

        val dOffset = dhParams.computeDOffset().length()

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

        val shoulderParam =
            DhParam(dhParams[0].d, toDegrees(newJointAngles[0]), dhParams[0].r, dhParams[0].alpha)

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
        val wristCenterToOrigin = dhParams.subList(0, 3).forwardKinematics(newJointAngles).getTranslation() -
            wristCenter
        val wristCenterToTip = target.getTranslation() - wristCenter
        val tipVector = target.getTranslation()

        val homeCenterToTip = dhParams.toFrameTransformation().getTranslation() -
            dhParams.subList(0, 4).toFrameTransformation().getTranslation()

        val homeCenterToOrigin = dhParams.subList(0, 4).toFrameTransformation().getTranslation() -
            dhParams.subList(0, 3).toFrameTransformation().getTranslation()

        // TODO: This needs to translate the elbow up to where the wrist link is, maybe up by r?
        val firstWristLink = dhParams.subList(0, 3).forwardKinematics(newJointAngles)
            .mult(getFrameTranslationMatrix(0, 0, 0))
            .getTranslation()

        val u1 = Vector3D(firstWristLink[0], firstWristLink[1], firstWristLink[2])
        val u2 = Vector3D(wristCenter[0], wristCenter[1], wristCenter[2])
        val v1 = Vector3D(wristCenter[0], wristCenter[1], wristCenter[2])
        val v2 = Vector3D(tipVector[0], tipVector[1], tipVector[2])

        val rotation = Rotation(u1, u2, v1, v2)
        val angles = rotation.getAngles(RotationOrder.XYX)

        // TODO: move frame to first wrist joint and orient it so that the first wrist's joint axes align with xyz
        /* We also need to make sure the target (the tip of the wrist) gets rotated in respect to this new frame
         * in order to do the next theta calculations, else they break down when the wrist axes aren't already aligned.
         */

        // uncomment this once frame gets rotated
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

        // TODO: tip rotation via the last joint, getting rest of wrist aligned will make this easy
//        newJointAngles[5] = jointSpaceVector[5]

        return newJointAngles.mapIndexed { index, elem ->
            if (index > 2) {
                toDegrees(elem)
            } else {
                toDegrees(elem) + dhParams[index].theta
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

/**
 * Compute the offset distance for a 3+ DOF arm shoulder joint, see Spong robot dynamics and control page 89-91.
 * This function assumes that the shoulder is located at the origin.
 *
 * @param chain the DH parameter chain for the arm
 *
 * @return a SimpleMatrix containing an x,y vector representing the origin offset for the shoulder
 */
fun ImmutableList<DhParam>.computeDOffset() : SimpleMatrix {
    val vectorA = immutableListOf(
            DhParam(this[0].d, 0, this[0].r, this[0].alpha),
            DhParam(this[1].d, 0, this[1].r, this[1].alpha),
            DhParam(this[2].d, 0, 0, this[2].alpha)
    ).forwardKinematics(arrayOf(0.0, 0.0, 0.0).toDoubleArray()).cols(3, 4).rows(0, 2)

    val vectorB = immutableListOf(
            DhParam(this[0].d, 0, this[0].r, this[0].alpha),
            DhParam(this[1].d, 0, this[1].r, this[1].alpha),
            DhParam(this[2].d, 0, 1, this[2].alpha)
    ).forwardKinematics(arrayOf(0.0, 0.0, 0.0).toDoubleArray()).cols(3, 4).rows(0, 2)

    val vA = vectorA - vectorB
    val vB = vectorB.negative()


    return vectorB + (vA.elementMult(vA.dot(vB) / vA.length().pow(2)))
}

fun ImmutableList<DhParam>.computeTheta1(
        wristCenter : SimpleMatrix) : ImmutableList<Double> = computeTheta1(wristCenter, 0.0)

fun ImmutableList<DhParam>.computeTheta1(wristCenter : SimpleMatrix, currentTheta1 : Double) : ImmutableList<Double> {
    val dOffset = this.computeDOffset()

    if (dOffset.length().absoluteValue < 0.001) {
        if(wristCenter[0, 3].absoluteValue < 0.001 && wristCenter[1, 3].absoluteValue < 0.001) {
            return ImmutableList.of(currentTheta1)
        }

        return ImmutableList.of(
                Math.atan2(wristCenter[1, 3], wristCenter[0, 3]),
                PI + Math.atan2(wristCenter[1, 3], wristCenter[0, 3])
        )
    }

    //left and right arm solutions, see spong pg. 90
    val phi = Math.atan2(wristCenter[1, 3], wristCenter[0, 3])
    val leftArmSolution = phi - Math.atan2(
            dOffset.length(),
            Math.sqrt(wristCenter[1, 3].pow(2) + wristCenter[0, 3].pow(2) - dOffset.length().pow(2))
    )

    val rightArmSolution = phi + Math.atan2(
            -dOffset.length(),
            -Math.sqrt(wristCenter[1, 3].pow(2) + wristCenter[0, 3].pow(2) - dOffset.length().pow(2))
    )

    return ImmutableList.of(leftArmSolution, rightArmSolution)
}

/**
 * Do forward kinematics on a list of DH parameters given a set of joint angles
 *
 * @param thetas an array of joint angles. The length of this array should match the length of the DH param list
 *
 * @return a simpleMatrix representing the frame transformation of the tip given the input joint angles
 */
fun ImmutableList<DhParam>.forwardKinematics(thetas: DoubleArray): SimpleMatrix {
    val paramList = ArrayList<DhParam>()

    for (i in 0 until this.size) {
        when (i) {
            0 -> paramList.add(
                    DhParam(
                            this[i].d,
                            toDegrees(thetas[i]),
                            this[i].r,
                            this[i].alpha
                    )
            )
            else -> paramList.add(
                    DhParam(
                            this[i].d,
                            -toDegrees(thetas[i]),
                            this[i].r,
                            this[i].alpha
                    )
            )
        }
    }

    return paramList.toImmutableList().toFrameTransformation()
}
