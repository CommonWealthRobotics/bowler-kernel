/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef

import au.edu.federation.caliko.FabrikBone3D
import au.edu.federation.caliko.FabrikChain3D
import au.edu.federation.caliko.FabrikJoint3D
import au.edu.federation.utils.Vec3f
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.dhparam.toFrameTransformation
import com.neuronrobotics.kinematicschef.util.getPointMatrix
import com.neuronrobotics.kinematicschef.util.getTranslation
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.ejml.simple.SimpleMatrix
import java.lang.Math.toDegrees
import kotlin.math.atan2

/**
 * A [DhInverseSolver] which uses Caliko's iterative solver.
 */
internal class CalikoInverseKinematicsEngine : DhInverseSolver {

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
    ): DoubleArray = inverseKinematicsWithError(target, jointSpaceVector, chain).first

    /**
     * Calculate the joint angles for the system.
     *
     * @param target The target frame transformation.
     * @param jointSpaceVector The current joint angles.
     * @param chain The DH params for the system.
     * @return The joint angles necessary to meet the target and the solve error.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun inverseKinematicsWithError(
        target: TransformNR,
        jointSpaceVector: DoubleArray,
        chain: DHChain
    ): Pair<DoubleArray, Float> {
        require(jointSpaceVector.size == chain.links.size) {
            "The joint angles and DH params must have equal size."
        }

        val fabrikChain = FabrikChain3D()
        fabrikChain.setFixedBaseMode(true)

        val dhParams = chain.toDhParams()
        dhParams.forEachIndexed { index, dhParam ->
            val boneLength: Float =
                if (index == dhParams.size - 1) {
                    defaultBoneLength
                } else {
                    calculateLinkLength(dhParam)
                }

            if (index == 0) {
                // The first link can't be added using addConsecutiveBone()
                fabrikChain.addBone(
                    FabrikBone3D(
                        Vec3f(0.0f),
                        FORWARD_AXIS.times(boneLength)
                    )
                )

                fabrikChain.setFreelyRotatingGlobalHingedBasebone(UP_AXIS)
            } else {
                // TODO: The directionUV could be X or Z depending on if we need to use d or r
                // TODO: Pull hardware limits from the DHChain
                fabrikChain.addConsecutiveFreelyRotatingHingedBone(
                    FORWARD_AXIS,
                    boneLength,
                    FabrikJoint3D.JointType.LOCAL_HINGE,
                    fabrikChain.chain[index - 1].directionUV.mult(dhParam.toFrameTransformation())
                )
            }
        }

        println("Before solving:")
        fabrikChain.chain.forEach { println(it) }
        val solveError = fabrikChain.solveForTarget(target.x, target.y, target.z)
        println("After solving:")
        fabrikChain.chain.forEach { println(it) }

        // Add a unit vector pointing up as the first element so we can get the angle of the first
        // link
        val directions = fabrikChain.chain.map { it.directionUV }.toMutableList()
        directions.add(0, baseUnitVector)

        val angles = mutableListOf<Double>()
        for (i in 0 until directions.size - 1) {
            val vec1 = directions[i]
            val vec2 = directions[i + 1]
            val projection = vec2.projectOntoPlane(vec1)
            angles.add(toDegrees(atan2(projection.y, projection.x).toDouble()).modulus(360.0))
        }

        return angles.map {
            if (!it.isFinite()) {
                0.0
            } else {
                it
            }
        }.toDoubleArray() to solveError
    }

    /**
     * Calculates the length of a link from its [DhParam].
     */
    private fun calculateLinkLength(dhParam: DhParam) =
        dhParam.length().toFloat().also {
            if (it == 0.0f) {
                defaultBoneLength
            } else {
                return it
            }
        }

    companion object {
        private const val defaultBoneLength = 10.0f
        private val baseUnitVector = Vec3f(0.0f, 0.0f, 1.0f).normalise()
        private val UP_AXIS = Vec3f(0.0f, 0.0f, 1.0f)
        private val FORWARD_AXIS = Vec3f(1.0f, 0.0f, 0.0f)
        private val RIGHT_AXIS = Vec3f(0.0f, 1.0f, 0.0f)
    }
}

private fun Vec3f.mult(ft: SimpleMatrix): Vec3f {
    val vecAsMat = getPointMatrix(x, y, z)
    val result = vecAsMat.mult(ft).getTranslation()
    return Vec3f(result[0].toFloat(), result[1].toFloat(), result[2].toFloat()).normalise()
}

private fun FabrikChain3D.solveForTarget(x: Number, y: Number, z: Number) =
    solveForTarget(x.toFloat(), y.toFloat(), z.toFloat())

private fun Double.modulus(rhs: Double) = (rem(rhs) + rhs).rem(rhs)
