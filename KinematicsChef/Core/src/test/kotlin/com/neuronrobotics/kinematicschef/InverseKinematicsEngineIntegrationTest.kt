/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef

import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.kinematicschef.dhparam.toFrameTransformation
import com.neuronrobotics.kinematicschef.util.toTransformNR
import com.neuronrobotics.sdk.addons.kinematics.DHLink
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InverseKinematicsEngineIntegrationTest {

    @Test
    fun `one invalid spherical wrist`() {
        val chain = TestUtil.makeMockChain(
            arrayListOf(
                DHLink(10.0, 0.0, 0.0, -90.0),
                DHLink(0.0, 0.0, 0.0, 90.0),
                DHLink(10.0, 0.0, 0.0, 0.0)
            )
        )

        val engine = InverseKinematicsEngine.getInstance()

        assertThrows<NotImplementedError> {
            engine.inverseKinematics(
                TransformNR(),
                listOf(0.0, 0.0, 0.0).toDoubleArray(),
                chain
            )
        }
    }

    @Test
    @Disabled
    fun `test cmm input arm`() {
        val cmmInputArm = ScriptingEngine.gitScriptRun(
            "https://gist.github.com/98892e87253005adbe4a.git",
            "TrobotMaster.xml",
            null
        ) as MobileBaseLoader

        val params = TestUtil.cmmInputArmDhParams

        val engine = InverseKinematicsEngine.getInstance()

        val jointAngles = engine.inverseKinematics(
            params.toFrameTransformation().toTransformNR(),
            listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray(),
            cmmInputArm.base.appendages[0].chain
        )

        println(jointAngles.joinToString())
    }
}
