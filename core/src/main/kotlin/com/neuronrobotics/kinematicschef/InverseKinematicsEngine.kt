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
import com.neuronrobotics.kinematicschef.classifier.ChainIdentifier
import com.neuronrobotics.kinematicschef.classifier.DefaultChainIdentifier
import com.neuronrobotics.kinematicschef.classifier.DefaultDhClassifier
import com.neuronrobotics.kinematicschef.classifier.DefaultWristIdentifier
import com.neuronrobotics.kinematicschef.classifier.DhClassifier
import com.neuronrobotics.kinematicschef.classifier.WristIdentifier
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.RevoluteJoint
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.dhparam.toFrameTransformation
import com.neuronrobotics.kinematicschef.util.elementMult
import com.neuronrobotics.kinematicschef.util.getFrameTranslationMatrix
import com.neuronrobotics.kinematicschef.util.getTranslation
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.length
import com.neuronrobotics.kinematicschef.util.modulus
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
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.absoluteValue
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
 * Project vectorB onto this vector
 */
fun SimpleMatrix.project(vectorB : SimpleMatrix) : SimpleMatrix? {
    if (!this.isVector) return null

    val vA = vectorB - this
    val vB = this.negative()

    return this + (vA.elementMult(vA.dot(vB) / vA.length().pow(2)))
}

fun SimpleMatrix.projectOntoPlane(planePoint : SimpleMatrix, normal : SimpleMatrix) : SimpleMatrix? {
    if (!this.isVector) return null

    if (normal.length() == 0.0) return this

    val a = normal[0]; val b = normal[1]; val c = normal[2]
    val d = planePoint[0]; val e = planePoint[1]; val f = planePoint[2]
    val x = this[0]; val y = this[1]; val z = this[2]

    val t = (a*d - a*x + b*e - b*y + c*f - c*z) /
            (a.pow(2) + b.pow(2) + c.pow(2))

    val projection = SimpleMatrix(3, 1)
    projection[0] = x + t*a
    projection[1] = y + t*b
    projection[2] = z + t*c

    return projection
}

/**
 * Compute the offset distance for a 3+ DOF arm shoulder joint, see Spong robot dynamics and control page 89-91.
 * This function assumes that the shoulder is located at the origin.
 *
 * @param chain the DH parameter chain for the arm
 *
 * @return a SimpleMatrix containing an x,y vector representing the origin offset for the shoulder
 */
fun ImmutableList<DhParam>.computeDOffset(theta1 : Double) : SimpleMatrix {
    val vectorA = immutableListOf(
            DhParam(this[0].d, 0, this[0].r, this[0].alpha),
            DhParam(this[1].d, 0, this[1].r, this[1].alpha),
            DhParam(this[2].d, 0, 0, this[2].alpha)
    ).forwardKinematics(arrayOf(theta1, 0.0, 0.0).toDoubleArray()).cols(3, 4).rows(0, 2)

    val vectorB = immutableListOf(
            DhParam(this[0].d, 0, this[0].r, this[0].alpha),
            DhParam(this[1].d, 0, this[1].r, this[1].alpha),
            DhParam(this[2].d, 0, 1, this[2].alpha)
    ).forwardKinematics(arrayOf(theta1, 0.0, 0.0).toDoubleArray()).cols(3, 4).rows(0, 2)

    return vectorB.project(vectorA) ?: SimpleMatrix(2, 1).also { it.zero() }
}

fun ImmutableList<DhParam>.computeDOffset() : SimpleMatrix = computeDOffset(0.0)

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

    return ImmutableList.of(leftArmSolution, leftArmSolution + PI, rightArmSolution, rightArmSolution + PI)
}

/**
 * Computes thetas 2 and 3 for a 6DOF arm with a spherical wrist. This function makes the following assumptions
 * about the joint configuration: the base is a shoulder joint, which is followed by two elbow joints, and joints 1
 * through 4 are not prismatic joints.
 *
 * @param wristCenter the desired coordinates for the wrist center to compute for
 * @param theta1 the computed joint angle of the first joint
 *
 * @return The set of solutions computed as a list of lists of joint angles {<t2, t3, t4>, <t2, t3, t4>, ...}
 */
fun ImmutableList<DhParam>.computeTheta23(wristCenter : SimpleMatrix, theta1 : Double)
        : ImmutableList<ImmutableList<Double>> {
    //vectors between joints
    val originTo2 = this.subList(0, 1).forwardKinematics(arrayOf(theta1).toDoubleArray()).cols(3, 4).rows(0, 3)
    val joint2To3 = (this.subList(0, 2).forwardKinematics(arrayOf(theta1, 0.0).toDoubleArray()) -
            this.subList(0, 1).forwardKinematics(arrayOf(theta1).toDoubleArray())).cols(3, 4).rows(0, 3)
    val joint3ToWristCenter = (this.subList(0, 4).forwardKinematics(arrayOf(theta1, 0.0, 0.0, 0.0).toDoubleArray()) -
            this.subList(0, 2).forwardKinematics(arrayOf(theta1, 0.0).toDoubleArray())).cols(3, 4).rows(0, 3)
    val joint3To4 = (this.subList(0, 3).forwardKinematics(arrayOf(theta1, 0.0, 0.0).toDoubleArray()) -
            this.subList(0, 2).forwardKinematics(arrayOf(theta1, 0.0).toDoubleArray())).cols(3, 4).rows(0, 3)

    val dOffset = this.computeDOffset(theta1)
    val offsetOrigin = SimpleMatrix(3, 1)
    offsetOrigin[0] = dOffset[0]
    offsetOrigin[1] = dOffset[1]
    offsetOrigin[2] = 0.0

    val offsetNormal = when {
        offsetOrigin.length() < 0.001 -> SimpleMatrix(3, 3).also {
            it[0, 0] = Math.cos(theta1 - PI/2)
            it[0, 1] = -Math.sin(theta1 - PI/2)
            it[1, 0] = Math.sin(theta1 - PI/2)
            it[1, 1] = Math.cos(theta1 - PI/2)
            it[2, 2] = 1.0
        }.mult(SimpleMatrix(3, 1).also { it.zero(); it[0] = 1.0 })

        else -> offsetOrigin.negative().divide(offsetOrigin.length())
    }

    val projectedOriginTo2 = (originTo2.projectOntoPlane(offsetOrigin, offsetNormal) as SimpleMatrix) - offsetOrigin
    val projectedWristCenter = (wristCenter.cols(3, 4).rows(0, 3)
            .projectOntoPlane(offsetOrigin, offsetNormal) as SimpleMatrix) - offsetOrigin - projectedOriginTo2

    val projected3ToCenter = (joint3ToWristCenter.projectOntoPlane(offsetOrigin, offsetNormal) as SimpleMatrix) - offsetOrigin
    val projected3To4 = (joint3To4.projectOntoPlane(offsetOrigin, offsetNormal) as SimpleMatrix) - offsetOrigin
    val projected2To3 = (joint2To3.projectOntoPlane(offsetOrigin, offsetNormal) as SimpleMatrix) - offsetOrigin

    val signTheta2 = {
        val vectorA = immutableListOf(
            DhParam(this[0].d, 0.0, this[0].r, this[0].alpha),
            DhParam(1.0, 0.0, 0.0, 0.0)
        ).forwardKinematics(arrayOf(0.0, 0.0, 0.0).toDoubleArray()).cols(3, 4).rows(0, 3)

        val vectorB = immutableListOf(
            DhParam(this[0].d, 0.0, this[0].r, this[0].alpha)
        ).forwardKinematics(arrayOf(0.0, 0.0, 0.0).toDoubleArray()).cols(3, 4).rows(0, 3)

        val vectorC = vectorA - vectorB
        Math.signum(if (vectorC[0] == 0.0) 1.0 else vectorC[0]) *
            Math.signum(if (vectorC[1] == 0.0) 1.0 else vectorC[1]) * -1.0
    }.invoke()

    val signTheta3 = {
        val vectorA = immutableListOf(
            DhParam(this[0].d, 0.0, this[0].r, this[0].alpha),
            DhParam(this[1].d, 0.0, this[1].r, this[1].alpha),
            DhParam(1.0, 0.0, 0.0, 0.0)
        ).forwardKinematics(arrayOf(0.0, 0.0, 0.0).toDoubleArray()).cols(3, 4).rows(0, 3)

        val vectorB = immutableListOf(
            DhParam(this[0].d, 0.0, this[0].r, this[0].alpha),
            DhParam(this[1].d, 0.0, this[0].r, this[1].alpha)
        ).forwardKinematics(arrayOf(0.0, 0.0, 0.0).toDoubleArray()).cols(3, 4).rows(0, 3)

        val vectorC = vectorA - vectorB
        Math.signum(if (vectorC[0] == 0.0) 1.0 else vectorC[0]) *
            Math.signum(if (vectorC[1] == 0.0) 1.0 else vectorC[1])
    }.invoke()

    val theta3Offset = Math.acos(
            (projected3To4.divide(projected3To4.length())).dot(projected3ToCenter.divide(projected3ToCenter.length()))
    )

    val r = Math.sqrt(projectedWristCenter[0].pow(2) + projectedWristCenter[1].pow(2))
    val s = projectedWristCenter[2]
    val bigD = (
            r.pow(2) + s.pow(2)
            - projected2To3.length().pow(2) - projected3ToCenter.length().pow(2)
        ) / (2 * projected2To3.length() * projected3ToCenter.length())

    val thetas3 = ImmutableList.of(
            Math.atan2(sqrt(1 - bigD.pow(2)), bigD),
            Math.atan2(-sqrt(1 - bigD.pow(2)), bigD)
    )

    val phi = Math.acos(
        (projected3ToCenter.length().pow(2)
            + projectedWristCenter.length().pow(2)
            - projected2To3.length().pow(2)
        ) /
        (2 * projected3ToCenter.length() * projectedWristCenter.length())
    )

    val projectedCenterToOriginUnit = SimpleMatrix(2, 1).also {
        val v3 = projectedWristCenter.negative().divide(projectedWristCenter.length())
        it[0] = v3[0]
        it[1] = v3[2]
    }

    val rot = SimpleMatrix(2, 2).also {
        it[0, 0] = Math.cos(phi)
        it[0, 1] = -Math.sin(phi)
        it[1, 0] = Math.sin(phi)
        it[1, 1] = Math.cos(phi)
    }

    val elbowDown = SimpleMatrix(3, 1).also {
        val v2 = rot.mult(projectedCenterToOriginUnit).elementMult(projected3ToCenter.length())
        it.zero()
        it[0] = v2[0]
        it[2] = v2[1]
    }
    val projected23ElbowDown = projectedWristCenter + elbowDown

    rot[0, 1] *= -1.0
    rot[1, 0] *= -1.0

    val elbowUp = SimpleMatrix(3, 1).also {
        val v2 = rot.mult(projectedCenterToOriginUnit).elementMult(projected3ToCenter.length())
        it.zero()
        it[0] = v2[0]
        it[2] = v2[1]
    }
    val projected23ElbowUp = projectedWristCenter + elbowUp

    val theta2ElbowUp = Math.acos(SimpleMatrix(2, 1).also { it[0] = 1.0; it[1] = 0.0 }
        .dot(projected23ElbowUp.divide(projected23ElbowUp.length())))
    val theta2ElbowDown = Math.acos(SimpleMatrix(2, 1).also { it[0] = 1.0; it[1] = 0.0 }
        .dot(projected23ElbowDown.divide(projected23ElbowDown.length())))

    /*val thetas2 = ImmutableList.of(
            Math.atan2(s, r) - Math.atan2(
                    projected3ToCenter.length()*Math.sin(thetas3[0]),
                    projected2To3.length() + projected3ToCenter.length() * Math.cos(thetas3[0])
            ),
            Math.atan2(s, r) - Math.atan2(
                    projected3ToCenter.length()*Math.sin(thetas3[1]),
                    projected2To3.length() + projected3ToCenter.length() * Math.cos(thetas3[1])
            )
    )*/

    return ImmutableList.of(
        ImmutableList.of(theta2ElbowUp * signTheta2, thetas3[0] * signTheta3 - theta3Offset * signTheta3),
        ImmutableList.of(theta2ElbowDown * signTheta2, thetas3[1] * signTheta3 - theta3Offset * signTheta3)
    )
}

fun ImmutableList<DhParam>.computeTheta456(
    target : SimpleMatrix,
    wristCenter : SimpleMatrix,
    theta1 : Double,
    theta2 : Double,
    theta3 : Double
) : ImmutableList<ImmutableList<Double>> {
    val wristOrigin = this.subList(0, 3).forwardKinematics(arrayOf(theta1, theta2, theta3).toDoubleArray())
        .cols(3, 4).rows(0, 3)

    val wristOriginToCenter = wristCenter.cols(3, 4).rows(0, 3) - wristOrigin
    val wristCenterToTarget = target.cols(3, 4).rows(0, 3) - wristCenter.cols(3, 4).rows(0, 3)

    val r = Math.sqrt(wristCenterToTarget[0].pow(2) + wristCenterToTarget[1].pow(2))
    val s = wristCenterToTarget[2]

    //if singularity condition, theta4 is free, set to 0
    val theta4 = if (Math.acos((wristOriginToCenter.divide(wristOriginToCenter.length())).dot(
            wristCenterToTarget.divide(wristCenterToTarget.length())
        )).absoluteValue < 0.001) {
        ImmutableList.of(0.0, 0.0)
    } else {
        val wristOriginToTarget = target.cols(3, 4).rows(0, 3) - wristOrigin
        ImmutableList.of(
            atan2(wristOriginToTarget[1], wristOriginToTarget[0]),
            atan2(wristOriginToTarget[1], wristOriginToTarget[0]) + PI/2
        )
    }

    val theta5 = ImmutableList.of(
        PI/2 + if (r.absoluteValue > 0.001) Math.atan(s/r) else 0.0,
        0.0 - PI/2 - if (r.absoluteValue > 0.001) Math.atan(s/r) else 0.0
    )

    val theta6 = ImmutableList.of(0.0, 0.0)

    return ImmutableList.of(
        ImmutableList.of(theta4[0], theta5[0], theta6[0]),
        ImmutableList.of(theta4[1], theta5[1], theta6[1])
    )
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
        paramList.add(DhParam(
            this[i].d,
            toDegrees(thetas[i]),
            this[i].r,
            this[i].alpha
        ))
    }

    return paramList.toImmutableList().toFrameTransformation()
}
