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
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.length
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
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * A [DhInverseSolver] which attempts to generate and cache an analytic solver by deriving the
 * chain's Euler angles. If an analytic solver cannot be generated, an iterative solver will be
 * used instead.
 */
class BowlerInverseKinematicsEngine
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
    ) = inverseKinematics(target.toSimpleMatrix(), jointSpaceVector, chain)

    fun inverseKinematics(
        target: SimpleMatrix,
        jointSpaceVector: DoubleArray,
        chain: DHChain
    ): DoubleArray {
        target[1, 3] = -14.0
        println("target: $target")
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

        val newJointAngles = jointSpaceVector.copyOf()

        val offsetFromShoulderCoRToWristCoR = abs(
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
                SimpleMatrix(2, 1).apply {
                    this[0, 0] = cos(toRadians(dhParams[0].alpha))
                    this[1, 0] = sin(toRadians(dhParams[0].alpha))
                }
            ))
        println("offsetFromShoulderCoRToWristCoR: $offsetFromShoulderCoRToWristCoR")

        val lengthFromShoulderToWristCenter = wristCenter.length()

        // from https://www.mathsisfun.com/algebra/trig-solving-sss-triangles.html
        val a = dhParams[2].r
        val b = dhParams[1].r

        val triangleSideA = acos(
            (b.pow(2) + lengthFromShoulderToWristCenter.pow(2) - a.pow(2)) /
                (2 * b * lengthFromShoulderToWristCenter)
        )

        val triangleSideB = acos(
            (lengthFromShoulderToWristCenter.pow(2) + a.pow(2) - b.pow(2)) /
                (2 * a * lengthFromShoulderToWristCenter)
        )

        val triangleSideC = PI - triangleSideA - triangleSideB // rule of triangles
        val elevation = asin(wristCenter[2] / lengthFromShoulderToWristCenter)

        // Angle of shoulder
        val angleFromFirstLinkToWristCoR = wrist.centerHomed(dhParams.subList(0, 3)).let {
            toDegrees(atan2(it[1], it[0]))
        }

        newJointAngles[0] = toDegrees(atan2(wristCenter[1], wristCenter[0])) -
            angleFromFirstLinkToWristCoR + dhParams[0].theta

        newJointAngles[1] = -toDegrees(triangleSideA + elevation + toRadians(dhParams[1].theta))

        if (dhParams[1].alpha.toInt() == 180) {
            // interior angle of the triangle, map to external angle
            newJointAngles[2] = toDegrees(triangleSideC) - 180 - dhParams[2].theta
        } else if (dhParams[1].alpha.toInt() == 0) {
            newJointAngles[2] = -toDegrees(triangleSideC) + dhParams[2].theta
        }

        if (dhParams.size > 3) {
            // keep it parallel
            // the wrist twist will always be 0 for this model because we assume that 3 links won't have a twist
            newJointAngles[3] = -(newJointAngles[1] + newJointAngles[2])
        } else if (dhParams.size > 4) {
            // keep the tool orientation parallel to the base
            // this link points in the same direction as the first link
            newJointAngles[4] = newJointAngles[0]
        }

        for (i in 0 until newJointAngles.size) {
            if (abs(newJointAngles[i]) < 0.01) {
                newJointAngles[i] = 0.0
            }
        }

        return newJointAngles
            .map { if (!it.isFinite()) 0.0 else it }
            .map { it.modulus(360) }
            .toDoubleArray()
            .also {
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
        internal fun bowlernewJointAngleserseKinematicsEngineModule() = module {
            bind<ChainIdentifier>().to<DefaultChainIdentifier>()
            bind<DhClassifier>().to<DefaultDhClassifier>()
            bind<WristIdentifier>().to<DefaultWristIdentifier>()
        }

        /**
         * Get an instance of the [BowlerInverseKinematicsEngine].
         */
        @JvmStatic
        fun getInstance(): BowlerInverseKinematicsEngine {
            return Guice.createInjector(bowlernewJointAngleserseKinematicsEngineModule())
                .getInstance(key<BowlerInverseKinematicsEngine>())
        }
    }
}
