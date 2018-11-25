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

internal class CalikoInverseKinematicsEngineTest {

    private val engine = CalikoInverseKinematicsEngine()

    @Test
    @Disabled
    fun `basic baxter left arm test`() {
        val chain = DHChain(object : AbstractKinematicsNR() {
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

        chain.addLink(DHLink(0.0, 0.0, 0.0, 0.0))
        chain.addLink(DHLink(0.0, -31.0 + 90, 69.0, -90.0))
        chain.addLink(DHLink(364.35, 0.0, 0.0, 90.0))
        chain.addLink(DHLink(0.0, 43.0, 69.0, -90.0))
        chain.addLink(DHLink(368.30, 0.0, 0.0, 90.0))
        chain.addLink(DHLink(0.0, 72.0, 10.0, -90.0))
        chain.addLink(DHLink(0.0, 0.0, 0.0, 90.0))

        val result = engine.inverseKinematics(
            TransformNR(0.0, 200.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            listOf(0.0, 0.0, 0.0).toDoubleArray(),
            chain
        )
        println(result)
    }
}
