/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef

import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.dhparam.toFrameTransformation
import com.neuronrobotics.kinematicschef.util.getFrameRotationMatrix
import com.neuronrobotics.kinematicschef.util.getFrameTranslationMatrix
import com.neuronrobotics.kinematicschef.util.getTranslation
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.toTransformNR
import com.neuronrobotics.sdk.addons.kinematics.DHLink
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import java.lang.Math.toRadians
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.test.assertEquals

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
    fun `test cmm input arm`() {
        val cmmInputArm = ScriptingEngine.gitScriptRun(
            "https://gist.github.com/NotOctogonapus/c3fc39308a506d4cb1cd7297193c41e7",
            "InputArmBase_copy.xml",
            null
        ) as? MobileBaseLoader ?: fail { "The script did not return a MobileBaseLoader." }

        val chain = cmmInputArm.base.appendages[0].chain

        val engine = BowlerInverseKinematicsEngine.getInstance()

        for (i in -180..180) {
            val params = chain.toDhParams()

            // The radius of the circle the target will move around
            val targetRadius = params[0].length / 2
            val targetHeight = params.toFrameTransformation().getTranslation()[2]

            val jointAngles = engine.inverseKinematics(
                getFrameTranslationMatrix(
                    targetRadius * cos(toRadians(i.toDouble())),
                    targetRadius * sin(toRadians(i.toDouble())),
                    targetHeight
                ),
                listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray(),
                chain
            )

            println(jointAngles.joinToString())

            // Test that the first link is correct. Need to remap the target angle according to the
            // wrist offset and the theta param on the link. The wrist offset is 120 degrees.
            assertTrue(abs(abs(i.toDouble() + 120 + params[0].theta) - jointAngles[0]) < 1)
        }
    }

    @Test
    @Disabled
    fun `test puma arm`() {
        val pumaArm = ScriptingEngine.gitScriptRun(
            "https://gist.github.com/NotOctogonapus/c3fc39308a506d4cb1cd7297193c41e7",
            "InputArmBase_copy.xml",
            null
        ) as? MobileBaseLoader ?: fail { "The script did not return a MobileBaseLoader." }

        val chain = pumaArm.base.appendages[1].chain
        val homeTarget = chain.toDhParams().toFrameTransformation()

        val tempTarget = immutableListOf(
            DhParam(0, 18.24, 0, -90),
            DhParam(14.9, 0, 43.2, 0),
            DhParam(0, 0, 2, 90),
            DhParam(43.2, 0, 0, -90),
            DhParam(0, 0, 0, 90),
            DhParam(5.6, 0, 0, 0)
        ).toFrameTransformation().toTransformNR()

        val engine = InverseKinematicsEngine.getInstance()

        val jointAngles = engine.inverseKinematics(
            chain.toDhParams().toFrameTransformation().toTransformNR(),
            listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray(),
            chain
        )

        println(jointAngles.joinToString())

//        for (i in 0..100) {
//            val jointAngles = engine.inverseKinematics(
//                homeTarget.mult(getFrameTranslationMatrix(i, 0, 0)).toTransformNR(),
//                listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray(),
//                chain
//            )
//
//            println(jointAngles.joinToString())
//        }

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

//    @Test
//    @Disabled
//    fun `test full spong arm`() {
//        val spongArm = ScriptingEngine.gitScriptRun(
//            "https://gist.github.com/NotOctogonapus/c3fc39308a506d4cb1cd7297193c41e7",
//            "InputArmBase_copy.xml",
//            null
//        ) as? MobileBaseLoader ?: fail { "The script did not return a MobileBaseLoader." }
//
//        val chain = spongArm.base.appendages[2].chain
//
//        val engine = InverseKinematicsEngine.getInstance()
//
//        for (i in 0..10) {
//            val jointAngles = engine.inverseKinematics(
//                chain.toDhParams().toFrameTransformation().toTransformNR().apply {
//                    x += i
//                },
//                listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray(),
//                chain
//            )
//
//            println("${jointAngles.joinToString()}\n")
//
//            chain.toDhParams().zip(jointAngles.toList()).map {
//                it.first.copy(theta = it.second)
//            }.let {
//                it.toFrameTransformation().print()
//            }
//        }
//    }
}
