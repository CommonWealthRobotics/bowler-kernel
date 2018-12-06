/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef

import com.neuronrobotics.sdk.addons.kinematics.DHLink
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InverseKinematicsEngineIntegrationTest {

    @Test
    fun `one invalid spherical wrist`() {
        val chain = TestUtil.makeMockChain().apply {
            addLink(DHLink(10.0, 0.0, 0.0, -90.0))
            addLink(DHLink(0.0, 0.0, 0.0, 90.0))
            addLink(DHLink(10.0, 0.0, 0.0, 0.0))
        }

        val engine = InverseKinematicsEngine.getInstance()

        val exception = assertThrows<UnsupportedOperationException> {
            engine.inverseKinematics(
                TransformNR(),
                listOf(0.0, 0.0, 0.0).toDoubleArray(),
                chain
            )
        }

        assertEquals(
            """
                |The wrist does not have Euler angles:
                |DhParam(d=10.0, theta=0.0, r=0.0, alpha=-90.0)
                |DhParam(d=0.0, theta=0.0, r=0.0, alpha=90.0)
                |DhParam(d=10.0, theta=0.0, r=0.0, alpha=0.0)
            """.trimMargin(),
            exception.message
        )
    }
}
