/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DHLink
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.lang.Math.toDegrees
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.test.assertEquals

internal class CalikoInverseKinematicsEngineTest {

    private val engine = CalikoInverseKinematicsEngine()

    private val mockChain = DHChain(object : AbstractKinematicsNR() {
        override fun forwardKinematics(p0: DoubleArray?): TransformNR {
            TODO("not implemented")
        }

        override fun disconnectDevice() {
            TODO("not implemented")
        }

        override fun inverseKinematics(p0: TransformNR?): DoubleArray {
            TODO("not implemented")
        }

        override fun connectDevice(): Boolean {
            TODO("not implemented")
        }
    })

    @Test
    fun `test 1 dof arm on x`() {
        mockChain.addLink(DHLink(10.0, 0.0, 0.0, 0.0))

        val target = 10
        val (angles, error) = engine.inverseKinematicsWithError(
            TransformNR().setX(target.toDouble()),
            listOf(0.0).toDoubleArray(),
            mockChain
        )

        assertAll(
            {
                assertEquals(1, angles.size, "Must have one joint angle")
            },
            {
                assertEquals(90, toDegrees(angles[0]).roundToInt(), "Joint angle must be 90")
            },
            {
                assertEquals(0, error.roundToInt(), "Solve error should equal 0")
            }
        )
    }

    @Test
    fun `test 1 dof arm on y`() {
        mockChain.addLink(DHLink(10.0, 0.0, 0.0, 0.0))

        val target = 10
        val (angles, error) = engine.inverseKinematicsWithError(
            TransformNR().setY(target.toDouble()),
            listOf(0.0).toDoubleArray(),
            mockChain
        )

        assertAll(
            {
                assertEquals(1, angles.size, "Must have one joint angle")
            },
            {
                assertEquals(90, toDegrees(angles[0]).roundToInt(), "Joint angle must be 90")
            },
            {
                assertEquals(0, error.roundToInt(), "Solve error should equal 0")
            }
        )
    }

    @Test
    fun `test 1 dof arm on z`() {
        mockChain.addLink(DHLink(10.0, 0.0, 0.0, 0.0))

        val target = 10
        val (angles, error) = engine.inverseKinematicsWithError(
            TransformNR().setZ(target.toDouble()),
            listOf(0.0).toDoubleArray(),
            mockChain
        )

        assertAll(
            {
                assertEquals(1, angles.size, "Must have one joint angle")
            },
            {
                assertEquals(0, toDegrees(angles[0]).roundToInt(), "Joint angle must be 0")
            },
            {
                assertEquals(target, error.roundToInt(), "Solve error should equal $target")
            }
        )
    }

    @Test
    @Disabled
    fun `basic baxter left arm test`() {
        mockChain.addLink(DHLink(0.0, 0.0, 0.0, 0.0))
        mockChain.addLink(DHLink(0.0, -31.0 + 90, 69.0, -90.0))
        mockChain.addLink(DHLink(364.35, 0.0, 0.0, 90.0))
        mockChain.addLink(DHLink(0.0, 43.0, 69.0, -90.0))
        mockChain.addLink(DHLink(368.30, 0.0, 0.0, 90.0))
        mockChain.addLink(DHLink(0.0, 72.0, 10.0, -90.0))
        mockChain.addLink(DHLink(0.0, 0.0, 0.0, 90.0))

        val baxterArmHypot = 69 + 364.35 + 374.29 + 368.3
        val triangeLegLength = baxterArmHypot / sqrt(2.0)
        val (result, error) = engine.inverseKinematicsWithError(
            TransformNR().setX(triangeLegLength).setY(triangeLegLength).setZ(270.35 - 69 - 10),
            listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray(),
            mockChain
        )
        println("Solve error: $error")
    }
}
