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
import au.edu.federation.utils.Vec3f
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import kotlin.math.acos
import kotlin.math.max

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
    ): DoubleArray {
        val fabrikChain = FabrikChain3D()
        fabrikChain.setFixedBaseMode(true)

        val dhParams = chain.toDhParams()
        dhParams.forEachIndexed { index, dhParam ->
            // TODO: Make this get the actual bone length
            val boneLength: Float =
                if (index == dhParams.size - 1) {
                    10.0f
                } else {
                    val possibleLength = dhParams[index + 1].run { max(d, r).toFloat() }
                    if (possibleLength == 0.0f) {
                        10.0f
                    } else {
                        possibleLength
                    }
                }

            // TODO: Transform the bone by the DhParam's frame transformation
            val bone = FabrikBone3D(
                Vec3f(0.0f),
                Vec3f(0.0f, boneLength, 0.0f)
            )

            if (index == 0) {
                fabrikChain.addBone(bone)
            } else {
                fabrikChain.addConsecutiveBone(bone)
            }
        }

        val solveError = fabrikChain.solveForTarget(target.x, target.y, target.z)
        println("Solve error: $solveError")
        val directions = fabrikChain.chain.map { it.directionUV }.toMutableList()
        // Add a unit vector point up so we can get the angle of the first link
        directions.add(0, Vec3f(0.0f, 0.0f, 1.0f))
        val angles = mutableListOf<Double>()
        for (i in 0 until directions.size - 1) {
            val vec1 = directions[i]
            val vec2 = directions[i + 1]
            angles.add(vec2.angle(vec1))
        }

        return angles.toDoubleArray()
    }
}

private fun Vec3f.angle(vec: Vec3f): Double =
    acos(dot(vec) / (length() * vec.length()))

private fun Vec3f.dot(vec: Vec3f): Double =
    (x * vec.x + y * vec.y + z * vec.z).toDouble()

private fun FabrikChain3D.solveForTarget(x: Number, y: Number, z: Number) =
    solveForTarget(x.toFloat(), y.toFloat(), z.toFloat())
