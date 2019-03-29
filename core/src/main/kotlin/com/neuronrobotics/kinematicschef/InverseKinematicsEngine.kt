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

import arrow.core.Try
import arrow.core.getOrDefault
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
import com.neuronrobotics.kinematicschef.util.angleBetweenVector
import com.neuronrobotics.kinematicschef.util.cross
import com.neuronrobotics.kinematicschef.util.elementMult
import com.neuronrobotics.kinematicschef.util.getRotationBetween
import com.neuronrobotics.kinematicschef.util.getTranslation
import com.neuronrobotics.kinematicschef.util.length
import com.neuronrobotics.kinematicschef.util.modulus
import com.neuronrobotics.kinematicschef.util.project
import com.neuronrobotics.kinematicschef.util.projectOntoPlane
import com.neuronrobotics.kinematicschef.util.toSimpleMatrix
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.ejml.simple.SimpleMatrix
import org.jlleitschuh.guice.key
import org.jlleitschuh.guice.module
import org.octogonapus.ktguava.collections.immutableListOf
import java.lang.Math.toDegrees
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.pow
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

        val wrist = chainElements.last() as? SphericalWrist ?: return noSolution(jointSpaceVector)

        val wristCenter = wrist.center(target)
        val newJointAngles = DoubleArray(jointSpaceVector.size) { 0.0 }

        val theta1s = dhParams.computeTheta1(wristCenter, jointSpaceVector[0])
        var solution: DoubleArray? = null

        for (theta1 in theta1s) {
            if (solution != null) break

            val theta23s = dhParams.computeTheta23(wristCenter, theta1)

            val theta456ElbowUp = dhParams.computeTheta456(
                target,
                wristCenter,
                theta1,
                theta23s[0][0],
                theta23s[0][1]
            )
            val theta456ElbowDown =
                dhParams.computeTheta456(
                    target,
                    wristCenter,
                    theta1,
                    theta23s[1][0],
                    theta23s[1][1]
                )

            val wristA = dhParams.forwardKinematics(
                arrayOf(
                    theta1,
                    theta23s[0][0],
                    theta23s[0][1],
                    theta456ElbowUp[0][0],
                    theta456ElbowUp[0][1],
                    theta456ElbowUp[0][2]
                ).toDoubleArray()
            ).getTranslation()

            val wristB = dhParams.forwardKinematics(
                arrayOf(
                    theta1,
                    theta23s[0][0],
                    theta23s[0][1],
                    theta456ElbowUp[1][0],
                    theta456ElbowUp[1][1],
                    theta456ElbowUp[1][2]
                ).toDoubleArray()
            ).getTranslation()

            val wristC = dhParams.forwardKinematics(
                arrayOf(
                    theta1,
                    theta23s[1][0],
                    theta23s[1][1],
                    theta456ElbowDown[0][0],
                    theta456ElbowDown[0][1],
                    theta456ElbowDown[0][2]
                ).toDoubleArray()
            ).getTranslation()

            val wristD = dhParams.forwardKinematics(
                arrayOf(
                    theta1,
                    theta23s[1][0],
                    theta23s[1][1],
                    theta456ElbowDown[1][0],
                    theta456ElbowDown[1][1],
                    theta456ElbowDown[1][2]
                ).toDoubleArray()
            ).getTranslation()

            when {
                (target.getTranslation() - wristC).length() < 0.001 -> {
                    newJointAngles[0] = theta1
                    newJointAngles[1] = theta23s[1][0]
                    newJointAngles[2] = theta23s[1][1]
                    newJointAngles[3] = theta456ElbowDown[0][0]
                    newJointAngles[4] = theta456ElbowDown[0][1]
                    newJointAngles[5] = theta456ElbowDown[0][2]
                    solution = newJointAngles
                }
                (target.getTranslation() - wristD).length() < 0.001 -> {
                    newJointAngles[0] = theta1
                    newJointAngles[1] = theta23s[1][0]
                    newJointAngles[2] = theta23s[1][1]
                    newJointAngles[3] = theta456ElbowDown[1][0]
                    newJointAngles[4] = theta456ElbowDown[1][1]
                    newJointAngles[5] = theta456ElbowDown[1][2]
                    solution = newJointAngles
                }
                (target.getTranslation() - wristA).length() < 0.001 -> {
                    newJointAngles[0] = theta1
                    newJointAngles[1] = theta23s[0][0]
                    newJointAngles[2] = theta23s[0][1]
                    newJointAngles[3] = theta456ElbowUp[0][0]
                    newJointAngles[4] = theta456ElbowUp[0][1]
                    newJointAngles[5] = theta456ElbowUp[0][2]
                    solution = newJointAngles
                }
                (target.getTranslation() - wristB).length() < 0.001 -> {
                    newJointAngles[0] = theta1
                    newJointAngles[1] = theta23s[0][0]
                    newJointAngles[2] = theta23s[0][1]
                    newJointAngles[3] = theta456ElbowUp[1][0]
                    newJointAngles[4] = theta456ElbowUp[1][1]
                    newJointAngles[5] = theta456ElbowUp[1][2]
                    solution = newJointAngles
                }
            }
        }

        if (solution == null) {
            return noSolution(jointSpaceVector)
        }

        return newJointAngles.mapIndexed { index, elem ->
            toDegrees(elem) - dhParams[index].theta
        }.map {
            if (it.isNaN())
                0.0.also { println("NaN found in jointSpaceVector. Replacing with 0.0") }
            else if (it >= 360 || it <= -360)
                it.modulus(360)
            else
                it
        }.toDoubleArray().also {
            println("jointAngles: ${it.joinToString()}")
        }
    }

    /**
     * Print that there was no solution and return the input joint angles.
     */
    private fun noSolution(jointSpaceVector: DoubleArray): DoubleArray {
        println("No solution found. Returning current joint angles.")
        return jointSpaceVector
    }

    companion object {
        private fun inverseKinematicsEngineModule() = module {
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
 * Compute the offset distance for a 3+ DOF arm shoulder joint, see Spong robot dynamics and
 * control page 89-91. This function assumes that the shoulder is located at the origin.
 *
 * @param theta1 The value for theta 1.
 * @return An x, y vector representing the origin offset for the shoulder.
 */
internal fun ImmutableList<DhParam>.computeDOffset(theta1: Double): SimpleMatrix {
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

    return Try {
        vectorB.project(vectorA)
    }.getOrDefault {
        SimpleMatrix(2, 1)
    }
}

internal fun ImmutableList<DhParam>.computeTheta1(
    wristCenter: SimpleMatrix,
    currentTheta1: Double = 0.0
): ImmutableList<Double> {
    val dOffset = computeDOffset(0.0)

    if (dOffset.length().absoluteValue < 0.001) {
        if (wristCenter[0].absoluteValue < 0.001 && wristCenter[1].absoluteValue < 0.001) {
            return immutableListOf(currentTheta1)
        }

        return immutableListOf(
            Math.atan2(wristCenter[1], wristCenter[0]),
            PI + Math.atan2(wristCenter[1], wristCenter[0])
        )
    }

    // left and right arm solutions, see spong pg. 90
    val phi = Math.atan2(wristCenter[1], wristCenter[0])
    val leftArmSolution = phi - Math.atan2(
        dOffset.length(),
        Math.sqrt(wristCenter[1].pow(2) + wristCenter[0].pow(2) - dOffset.length().pow(2))
    )

    val rightArmSolution = phi + Math.atan2(
        -dOffset.length(),
        -Math.sqrt(wristCenter[1].pow(2) + wristCenter[0].pow(2) - dOffset.length().pow(2))
    )

    return immutableListOf(
        leftArmSolution,
        leftArmSolution + PI,
        rightArmSolution,
        rightArmSolution + PI
    )
}

/**
 * Computes thetas 2 and 3 for a 6DOF arm with a spherical wrist. This function makes the
 * following assumptions about the joint configuration: the base is a shoulder joint, which is
 * followed by two elbow joints, and joints 1 through 4 are not prismatic joints.
 *
 * @param wristCenter The desired coordinates for the wrist center to compute for.
 * @param theta1 The computed joint angle of the first joint.
 * @return The set of solutions computed as a list of lists of joint angles {<t2, t3, t4>, <t2,
 * t3, t4>, ...}.
 */
internal fun ImmutableList<DhParam>.computeTheta23(
    wristCenter: SimpleMatrix,
    theta1: Double
): ImmutableList<ImmutableList<Double>> {
    // vectors between joints
    val originTo2 =
        this.subList(0, 1).forwardKinematics(arrayOf(0.0).toDoubleArray()).getTranslation()
    val joint2To3 = (this.subList(0, 2).forwardKinematics(arrayOf(0.0, 0.0).toDoubleArray()) -
        this.subList(0, 1).forwardKinematics(arrayOf(0.0).toDoubleArray())).getTranslation()
    val joint3ToWristCenter =
        (this.subList(0, 4).forwardKinematics(arrayOf(0.0, 0.0, 0.0, 0.0).toDoubleArray()) -
            this.subList(0, 2).forwardKinematics(
                arrayOf(
                    0.0,
                    0.0
                ).toDoubleArray()
            )).getTranslation()
    val joint3To4 = (this.subList(0, 3).forwardKinematics(arrayOf(0.0, 0.0, 0.0).toDoubleArray()) -
        this.subList(0, 2).forwardKinematics(arrayOf(0.0, 0.0).toDoubleArray())).getTranslation()

    val theta1Rotation = SimpleMatrix(3, 3).also {
        it[0, 0] = Math.cos(-theta1)
        it[0, 1] = -Math.sin(-theta1)
        it[1, 0] = Math.sin(-theta1)
        it[1, 1] = Math.cos(-theta1)
        it[2, 2] = 1.0
    }

    val rotatedWristCenter = theta1Rotation.mult(wristCenter)

    val dOffset = this.computeDOffset(0.0)
    val offsetOrigin = SimpleMatrix(3, 1)
    offsetOrigin[0] = dOffset[0]
    offsetOrigin[1] = dOffset[1]
    offsetOrigin[2] = 0.0

    val offsetNormal = when {
        offsetOrigin.length() < 0.001 -> SimpleMatrix(3, 3).also {
            it[0, 0] = Math.cos(-PI / 2)
            it[0, 1] = -Math.sin(-PI / 2)
            it[1, 0] = Math.sin(-PI / 2)
            it[1, 1] = Math.cos(-PI / 2)
            it[2, 2] = 1.0
        }.mult(SimpleMatrix(3, 1).also { it[0] = 1.0 })

        else -> offsetOrigin.negative().divide(offsetOrigin.length())
    }

    val projectedOriginTo2 = originTo2.projectOntoPlane(offsetOrigin, offsetNormal) - offsetOrigin
    val projectedWristCenter = rotatedWristCenter
        .projectOntoPlane(offsetOrigin, offsetNormal) - offsetOrigin - projectedOriginTo2

    val projected3ToCenter =
        joint3ToWristCenter.projectOntoPlane(offsetOrigin, offsetNormal) - offsetOrigin
    val projected3To4 = joint3To4.projectOntoPlane(offsetOrigin, offsetNormal) - offsetOrigin
    val projected2To3 = joint2To3.projectOntoPlane(offsetOrigin, offsetNormal) - offsetOrigin

    val signTheta2 = run {
        val vectorA = immutableListOf(
            DhParam(this[0].d, 0.0, this[0].r, this[0].alpha),
            DhParam(1.0, 0.0, 0.0, 0.0)
        ).forwardKinematics(arrayOf(0.0, 0.0, 0.0).toDoubleArray()).getTranslation()

        val vectorB = immutableListOf(
            DhParam(this[0].d, 0.0, this[0].r, this[0].alpha)
        ).forwardKinematics(arrayOf(0.0, 0.0, 0.0).toDoubleArray()).getTranslation()

        val vectorC = vectorA - vectorB
        Math.signum(if (vectorC[0] == 0.0) 1.0 else vectorC[0]) *
            Math.signum(if (vectorC[1] == 0.0) 1.0 else vectorC[1]) * -1.0
    }

    val vectorA = immutableListOf(
        DhParam(this[0].d, 0.0, this[0].r, this[0].alpha),
        DhParam(this[1].d, 0.0, this[1].r, this[1].alpha),
        DhParam(1.0, 0.0, 0.0, 0.0)
    ).forwardKinematics(arrayOf(0.0, 0.0, 0.0).toDoubleArray()).getTranslation()

    val vectorB = immutableListOf(
        DhParam(this[0].d, 0.0, this[0].r, this[0].alpha),
        DhParam(this[1].d, 0.0, this[0].r, this[1].alpha)
    ).forwardKinematics(arrayOf(0.0, 0.0, 0.0).toDoubleArray()).getTranslation()

    val vectorC = vectorA - vectorB
    val signTheta3 = Math.signum(if (vectorC[1] == 0.0) 1.0 else vectorC[1])

    val signTheta3Offset = run {
        val projection34To3Center = projected3To4.project(projected3ToCenter)
        val distance = projected3ToCenter.divide(projected3ToCenter.length())
            .elementMult(projection34To3Center.length()).minus(projected3To4)

        when {
            distance[2] > 0.0 -> -1.0
            distance[2] < 0.0 -> 1.0
            else -> when {
                distance[0] > 0.0 -> -1.0
                else -> 1.0
            }
        }
    }

    val theta3Offset = Math.acos(
        projected3To4.divide(projected3To4.length()).dot(
            projected3ToCenter.divide(projected3ToCenter.length())
        )
    ) * signTheta3Offset

    val r = Math.sqrt(projectedWristCenter[0].pow(2) + projectedWristCenter[1].pow(2))
    val s = projectedWristCenter[2]
    val bigD = (
        r.pow(2) + s.pow(2) -
            projected2To3.length().pow(2) - projected3ToCenter.length().pow(2)
        ) / (2 * projected2To3.length() * projected3ToCenter.length())

    val thetas3 = immutableListOf(
        Math.atan2(sqrt(1 - bigD.pow(2)), bigD),
        Math.atan2(-sqrt(1 - bigD.pow(2)), bigD)
    )

    val phi = Math.acos(
        (projected3ToCenter.length().pow(2) + projectedWristCenter.length().pow(2) -
            projected2To3.length().pow(2)) / (2 * projected3ToCenter.length() *
            projectedWristCenter.length())
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
        it[0] = v2[0]
        it[2] = v2[1]
    }

    val projected23ElbowDown = projectedWristCenter + elbowDown

    rot[0, 1] *= -1.0
    rot[1, 0] *= -1.0

    val elbowUp = SimpleMatrix(3, 1).also {
        val v2 = rot.mult(projectedCenterToOriginUnit).elementMult(projected3ToCenter.length())
        it[0] = v2[0]
        it[2] = v2[1]
    }
    val projected23ElbowUp = projectedWristCenter + elbowUp

    val theta2ElbowUp = Math.signum(projected23ElbowUp[2]) *
        Math.acos(SimpleMatrix(2, 1).also { it[0] = 1.0; it[1] = 0.0 }
            .dot(projected23ElbowUp.divide(projected23ElbowUp.length())))

    val theta2ElbowDown = Math.signum(projected23ElbowDown[2]) *
        Math.acos(SimpleMatrix(2, 1).also { it[0] = 1.0; it[1] = 0.0 }
            .dot(projected23ElbowDown.divide(projected23ElbowDown.length())))

    /*val thetas2 = immutableListOf(
            Math.atan2(s, r) - Math.atan2(
                    projected3ToCenter.length()*Math.sin(thetas3[0]),
                    projected2To3.length() + projected3ToCenter.length() * Math.cos(thetas3[0])
            ),
            Math.atan2(s, r) - Math.atan2(
                    projected3ToCenter.length()*Math.sin(thetas3[1]),
                    projected2To3.length() + projected3ToCenter.length() * Math.cos(thetas3[1])
            )
    )*/

    return immutableListOf(
        immutableListOf(
            theta2ElbowUp * signTheta2,
            thetas3[0] * signTheta3 - theta3Offset * signTheta3
        ),
        immutableListOf(
            theta2ElbowDown * signTheta2,
            thetas3[1] * signTheta3 - theta3Offset * signTheta3
        )
    )
}

internal fun ImmutableList<DhParam>.computeTheta456(
    target: SimpleMatrix,
    wristCenter: SimpleMatrix,
    theta1: Double,
    theta2: Double,
    theta3: Double
): ImmutableList<ImmutableList<Double>> {

    val wristOrigin =
        this.subList(0, 3).forwardKinematics(arrayOf(theta1, theta2, theta3).toDoubleArray())
            .getTranslation()

    val zUnit = SimpleMatrix(3, 1).also { it[2] = 1.0 }

    val originToCenter = wristCenter - wristOrigin
    val originRot = zUnit.getRotationBetween(originToCenter)

    // val wristOriginToCenter = originRot.mult(originToCenter)
    val wristCenterToTarget = originRot.mult(target.getTranslation() - wristCenter)

    val r = Math.sqrt(wristCenterToTarget[0].pow(2) + wristCenterToTarget[1].pow(2))
    val s = wristCenterToTarget[2]

    // if singularity condition, theta4 is free, set to 0
    val theta4 = if (r < 0.001) {
        immutableListOf(0.0, 0.0)
    } else {
        val wristOriginToTarget = originRot.mult(target.getTranslation() - wristOrigin)
        immutableListOf(
            atan2(wristOriginToTarget[1], wristOriginToTarget[0]) + PI / 2,
            atan2(wristOriginToTarget[1], wristOriginToTarget[0]) - PI / 2
        )
    }

    val theta5 = immutableListOf(
        if (r.absoluteValue > 0.001) Math.atan(s / r) - PI / 2 else 0.0,
        if (r.absoluteValue > 0.001) PI / 2 - Math.atan(s / r) else 0.0
    )

    val targetDirVector = target.mult(SimpleMatrix(4, 1).also {
        it[0] = 1.0
        it[3] = 1.0
    }) - target.cols(3, 4).rows(0, 4)

    val targetDirNormal = (target.mult(SimpleMatrix(4, 1).also {
        it[1] = 1.0
        it[3] = 1.0
    }) - target.cols(3, 4).rows(0, 4)).cross(targetDirVector)

    val currentDirVectors = immutableListOf(
        immutableListOf(
            this[0], this[1], this[2], this[3], this[4], this[5],
            DhParam(0.0, 0.0, 1.0, 0.0)
        ).forwardKinematics(
            arrayOf(
                theta1, theta2, theta3, theta4[0], theta5[0], 0.0, 0.0
            ).toDoubleArray()
        ).cols(3, 4).rows(0, 4) - target.cols(3, 4).rows(0, 4),

        immutableListOf(
            this[0], this[1], this[2], this[3], this[4], this[5],
            DhParam(0.0, 0.0, 1.0, 0.0)
        ).forwardKinematics(
            arrayOf(
                theta1, theta2, theta3, theta4[1], theta5[1], 0.0, 0.0
            ).toDoubleArray()
        ).cols(3, 4).rows(0, 4) - target.cols(3, 4).rows(0, 4)
    )

    val cross = immutableListOf(
        targetDirVector.cross(currentDirVectors[0]),
        targetDirVector.cross(currentDirVectors[1])
    )

    fun computePossibleTheta6(currentDirVector: SimpleMatrix) =
        if ((targetDirNormal - cross[0]).length() < 0.001) 1.0 else -1.0 *
            if ((targetDirVector - currentDirVector).length() < 0.001)
                0.0
            else
                targetDirVector.angleBetweenVector(currentDirVector)

    val theta6 = immutableListOf(
        computePossibleTheta6(currentDirVectors[0]),
        computePossibleTheta6(currentDirVectors[1])
    )

    return immutableListOf(
        immutableListOf(theta4[0], theta5[0], theta6[0]),
        immutableListOf(theta4[1], theta5[1], theta6[1])
    )
}

/**
 * Do forward kinematics on a list of DH parameters given a set of joint angles.
 *
 * @param thetas An array of joint angles. The length of this array should match the length of the DH param list.
 * @return The frame transformation of the tip given the input joint angles.
 */
fun ImmutableList<DhParam>.forwardKinematics(thetas: DoubleArray): SimpleMatrix {
    val paramList = mutableListOf<DhParam>()

    for (i in 0 until this.size) {
        paramList.add(this[i].copy(theta = toDegrees(thetas[i])))
    }

    return paramList.toFrameTransformation()
}
