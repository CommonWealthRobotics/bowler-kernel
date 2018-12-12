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
import com.neuronrobotics.kinematicschef.util.getFrameTranslationMatrix
import com.neuronrobotics.kinematicschef.util.getRotation
import com.neuronrobotics.kinematicschef.util.getTranslation
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.length
import com.neuronrobotics.kinematicschef.util.modulus
import com.neuronrobotics.kinematicschef.util.projectionOntoPlane
import com.neuronrobotics.kinematicschef.util.projectionOntoVector
import com.neuronrobotics.kinematicschef.util.toSimpleMatrix
import com.neuronrobotics.kinematicschef.util.toTranslation
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.ejml.simple.SimpleMatrix
import org.jlleitschuh.guice.key
import org.jlleitschuh.guice.module
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.asin
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

        // Angle of shoulder
        val angleFromFirstLinkToWristCoR = wrist.centerHomed(dhParams.subList(0, 3)).let {
            toDegrees(atan2(it[1], it[0]))
        }

        newJointAngles[0] = toDegrees(atan2(wristCenter[1], wristCenter[0])) -
            angleFromFirstLinkToWristCoR + dhParams[0].theta

        val (elbow1, elbow2) = solveElbows(dhParams, wristCenter, target)
        newJointAngles[1] = elbow1 + dhParams[1].theta
        newJointAngles[2] = elbow2 + dhParams[2].theta

        return newJointAngles
            .map { if (!it.isFinite()) 0.0 else it }
            .map { it.modulus(360) }
            .toDoubleArray()
            .also {
                println("jointAngles: ${it.joinToString()}")
            }
    }

    private fun solveElbows(
        dhParams: ImmutableList<DhParam>,
        wristCenter: SimpleMatrix,
        target: SimpleMatrix
    ): Pair<Double, Double> {
//        val target = wristCenter.toTranslation()
//        val Cx = target[0]
//        val Cy = target[2]
//
//        val AC = sqrt(Cx.pow(2) + Cy.pow(2))
//        val ac_angle = atan2(Cy, Cx)
//        val AB = dhParams[1].r
//        val BC = dhParams[2].r
//
//        if (AB + BC <= AC || AB + AC <= BC || BC + AC <= AB) {
//            println("Elbow solution not possible")
//        }
//
//        val littleS = (AB + BC + AC) / 2
//        val S_squared = littleS * (littleS - AB) * (littleS - BC) * (littleS - AC)
//        val S = if (abs(S_squared) < 1e-6) 0.0 else sqrt(S_squared)
//
//        val A = asin((2 * S) / (AB * AC))
//        val B = asin((2 * S) / (AB * BC))
//        val C = asin((2 * S) / (AC * BC))
//
//        return toDegrees(ac_angle + A) to toDegrees(B)
        val wristOriented = target.getRotation().mult(wristCenter)

        val elbow2Position = wristCenter.toTranslation().mult(
            dhParams[3].frameTransformation.invert()
        ).getTranslation()

        val elbow2Angle = toDegrees(
            atan2(
                elbow2Position[2], elbow2Position[0]
            )
        )
        TODO()
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
