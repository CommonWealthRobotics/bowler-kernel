/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef

import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.dhparam.toFrameTransformation
import com.neuronrobotics.kinematicschef.util.getFrameTranslationMatrix
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
//            "https://gist.github.com/98892e87253005adbe4a.git",
//            "TrobotMaster.xml",
            "https://gist.github.com/NotOctogonapus/c3fc39308a506d4cb1cd7297193c41e7",
            "InputArmBase_copy.xml",
            null
        ) as MobileBaseLoader

        val chain = cmmInputArm.base.appendages[0].chain

        val engine = InverseKinematicsEngine.getInstance()

        val jointAngles = engine.inverseKinematics(
            chain.toDhParams().toFrameTransformation().toTransformNR(),
            listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray(),
            chain
        )

        println(jointAngles.joinToString())
    }

    @Test
    @Disabled
    fun `test puma arm`() {
        val pumaArm = ScriptingEngine.gitScriptRun(
            "https://gist.github.com/NotOctogonapus/c3fc39308a506d4cb1cd7297193c41e7",
            "InputArmBase_copy.xml",
            null
        ) as MobileBaseLoader

        val chain = pumaArm.base.appendages[1].chain
        val homeTarget = chain.toDhParams().toFrameTransformation()

        val engine = InverseKinematicsEngine.getInstance()

        for (i in 0..100) {
            val jointAngles = engine.inverseKinematics(
                homeTarget.mult(getFrameTranslationMatrix(i, 0, 0)).toTransformNR(),
                listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray(),
                chain
            )

            println(jointAngles.joinToString())
        }

//        for (i in -100..100) {
//            for (j in -100..100) {
//                for (k in -100..100) {
//                    val jointAngles = engine.inverseKinematics(
//                        homeTarget.mult(getFrameTranslationMatrix(i, 0, 0)).toTransformNR(),
//                        listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray(),
//                        chain
//                    )
//                    if (jointAngles[0] != 0.0) {
//                        println(jointAngles.joinToString())
//                    }
//                }
//            }
//        }
    }
}
