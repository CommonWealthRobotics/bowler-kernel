/*
 * This file is part of kinematics-chef.
 *
 * kinematics-chef is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kinematics-chef is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with kinematics-chef.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.kinematicschef

class InverseKinematicsEngineIntegrationTest {

    // Used to check for SimpleMatrix equality
    private val equalityTolerance = 1e-8

//    @Test
//    @Disabled("computeTheta23 can't handle only 3 params")
//    fun `one invalid spherical wrist`() {
//        val params = immutableListOf(
//            DhParam(10.0, 0.0, 0.0, -90.0),
//            DhParam(0.0, 0.0, 0.0, 90.0),
//            DhParam(10.0, 0.0, 0.0, 0.0)
//        )
//
//        val engine = InverseKinematicsEngine.getInstance()
//
//        assertThrows<NotImplementedError> {
//            engine.inverseKinematics(
//                SimpleMatrix.identity(4),
//                DoubleArray(3) { 0.0 },
//                params
//            )
//        }
//    }
//
//    @Test
//    fun `test cmm input arm global zero`() {
//        val engine = InverseKinematicsEngine.getInstance()
//
//        val target = cmmInputArmDhParams.forwardKinematics(
//            arrayOf(
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0
//            ).toDoubleArray()
//        )
//
//        val jointAngles = engine.inverseKinematics(
//            target,
//            DoubleArray(6) { 0.0 },
//            cmmInputArmDhParams
//        )
//
//        val fkTip = cmmInputArmDhParams.forwardKinematics(
//            arrayOf(
//                (jointAngles[0] + cmmInputArmDhParams[0].theta) * PI / 180,
//                (jointAngles[1] + cmmInputArmDhParams[1].theta) * PI / 180,
//                (jointAngles[2] + cmmInputArmDhParams[2].theta) * PI / 180,
//                (jointAngles[3] + cmmInputArmDhParams[3].theta) * PI / 180,
//                (jointAngles[4] + cmmInputArmDhParams[4].theta) * PI / 180,
//                (jointAngles[5] + cmmInputArmDhParams[5].theta) * PI / 180
//            ).toDoubleArray()
//        )
//
//        val targetVec = target.cols(3, 4).rows(0, 3)
//        val tipVec = fkTip.cols(3, 4).rows(0, 3)
//
//        assert((targetVec - tipVec).length() < 0.001)
//    }
//
//    @Test
//    @Disabled("Broken.")
//    fun `test IK with many theta permutations`() {
//        val engine = InverseKinematicsEngine.getInstance()
//        val thetaIncrement = 2 * PI / 4
//        for (i in (0.0..2 * PI).step(thetaIncrement)) {
//            val target = cmmInputArmDhParams.forwardKinematics(
//                DoubleArray(6) { i }
//            )
//
//            val thetasFromIk = engine.inverseKinematics(
//                target,
//                DoubleArray(6) { 0.0 },
//                cmmInputArmDhParams
//            )
//
//            val targetUsingThetasFromIk = cmmInputArmDhParams.forwardKinematics(
//                thetasFromIk.mapIndexed { index, theta ->
//                    toRadians(theta + cmmInputArmDhParams[index].theta)
//                }.toDoubleArray()
//            )
//
//            assertTrue(target.isIdentical(targetUsingThetasFromIk, equalityTolerance))
//        }
//    }
//
//    @Test
//    fun `test cmm input arm home`() {
//        val engine = InverseKinematicsEngine.getInstance()
//        val target = cmmInputArmDhParams.forwardKinematics(
//            arrayOf(
//                cmmInputArmDhParams[0].theta * PI / 180,
//                cmmInputArmDhParams[1].theta * PI / 180,
//                cmmInputArmDhParams[2].theta * PI / 180,
//                cmmInputArmDhParams[3].theta * PI / 180,
//                cmmInputArmDhParams[4].theta * PI / 180,
//                cmmInputArmDhParams[5].theta * PI / 180
//            ).toDoubleArray()
//        )
//
//        val jointAngles = engine.inverseKinematics(
//            target,
//            DoubleArray(6) { 0.0 },
//            cmmInputArmDhParams
//        )
//
//        val fkTip = cmmInputArmDhParams.forwardKinematics(
//            arrayOf(
//                (jointAngles[0] + cmmInputArmDhParams[0].theta) * PI / 180,
//                (jointAngles[1] + cmmInputArmDhParams[1].theta) * PI / 180,
//                (jointAngles[2] + cmmInputArmDhParams[2].theta) * PI / 180,
//                (jointAngles[3] + cmmInputArmDhParams[3].theta) * PI / 180,
//                (jointAngles[4] + cmmInputArmDhParams[4].theta) * PI / 180,
//                (jointAngles[5] + cmmInputArmDhParams[5].theta) * PI / 180
//            ).toDoubleArray()
//        )
//
//        val targetVec = target.cols(3, 4).rows(0, 3)
//        val tipVec = fkTip.cols(3, 4).rows(0, 3)
//
//        assert((targetVec - tipVec).length() < 0.001)
//    }
//
//    @Test
//    fun `test cmm input arm old`() {
//        val engine = InverseKinematicsEngine.getInstance()
//
//        fun testTheta1OnRadius(targetRadius: Double) {
//            for (i in -180.0..180.0 step 0.1) {
//                val targetHeight = cmmInputArmDhParams.toFrameTransformation().getTranslation()[2]
//                val target = getFrameTranslationMatrix(
//                    targetRadius * cos(toRadians(i)),
//                    targetRadius * sin(toRadians(i)),
//                    targetHeight
//                )
//
//                val jointAngles = engine.inverseKinematics(
//                    target,
//                    DoubleArray(6) { 0.0 },
//                    cmmInputArmDhParams
//                )
//
//                val chainWithAngles = cmmInputArmDhParams.mapIndexed { index, elem ->
//                    // Offset theta by the joint angle because the joint angle is offset back by theta
//                    elem.copy(theta = (elem.theta + jointAngles[index]).let {
//                        if (it > 360 || it < -360)
//                            it.modulus(360)
//                        else
//                            it
//                    })
//                }
//
//                assertTrue(
//                    target.getTranslation().isIdentical(
//                        chainWithAngles.toFrameTransformation().getTranslation(),
//                        equalityTolerance
//                    )
//                )
//            }
//        }
//
//        fun testThetasHomed() {
//            val jointAngles = engine.inverseKinematics(
//                cmmInputArmDhParams.toFrameTransformation(),
//                DoubleArray(6) { 0.0 },
//                cmmInputArmDhParams
//            )
//
//            val chainWithAngles = cmmInputArmDhParams.mapIndexed { index, elem ->
//                // Offset theta by the joint angle because the joint angle is offset back by theta
//                elem.copy(theta = elem.theta + jointAngles[index])
//            }
//
//            assertTrue(
//                cmmInputArmDhParams.toFrameTransformation().isIdentical(
//                    chainWithAngles.toFrameTransformation(),
//                    equalityTolerance
//                )
//            )
//        }
//
//        fun testTheta1OnXAxis() {
//            for (sign in listOf(-1, 1)) {
//                val targetHeight = cmmInputArmDhParams.toFrameTransformation().getTranslation()[2]
//                val tipHome = cmmInputArmDhParams.toFrameTransformation().getTranslation()
//                val targetFrame = getFrameTranslationMatrix(
//                    sign * sqrt(tipHome[0].pow(2) + tipHome[1].pow(2)),
//                    0,
//                    targetHeight
//                )
//
//                val jointAngles = engine.inverseKinematics(
//                    targetFrame,
//                    DoubleArray(6) { 0.0 },
//                    cmmInputArmDhParams
//                )
//
//                val chainWithAngles = cmmInputArmDhParams.mapIndexed { index, elem ->
//                    // Offset theta by the joint angle because the joint angle is offset back by theta
//                    elem.copy(theta = (elem.theta + jointAngles[index]).let {
//                        if (it > 360 || it < -360)
//                            it.modulus(360)
//                        else
//                            it
//                    })
//                }
//                println("ch: ${chainWithAngles.joinToString()}")
//
//                assertTrue(
//                    targetFrame.getTranslation().isIdentical(
//                        chainWithAngles.toFrameTransformation().getTranslation(),
//                        equalityTolerance
//                    )
//                )
//            }
//        }
//
//        fun testThetasAlongXAxis() {
//            for (i in -10..10) {
//                val targetHeight = cmmInputArmDhParams.toFrameTransformation().getTranslation()[2]
//                val targetFrame = getFrameTranslationMatrix(
//                    i,
//                    0,
//                    targetHeight
//                )
//
//                val jointAngles = engine.inverseKinematics(
//                    targetFrame,
//                    DoubleArray(6) { 0.0 },
//                    cmmInputArmDhParams
//                )
//
//                val chainWithAngles = cmmInputArmDhParams.mapIndexed { index, elem ->
//                    // Offset theta by the joint angle because the joint angle is offset back by theta
//                    elem.copy(theta = (elem.theta + jointAngles[index]).let {
//                        if (it > 360 || it < -360)
//                            it.modulus(360)
//                        else
//                            it
//                    })
//                }
//                println("ch: ${chainWithAngles.joinToString()}")
//
//                assertTrue(
//                    targetFrame.getTranslation().isIdentical(
//                        chainWithAngles.toFrameTransformation().getTranslation(),
//                        equalityTolerance
//                    )
//                )
//            }
//        }
//
//        val tipHome = cmmInputArmDhParams.toFrameTransformation().getTranslation()
//        val lengthToTip = sqrt(tipHome[0].pow(2) + tipHome[1].pow(2))
//
//        testTheta1OnRadius(lengthToTip) // The radius for the home position
//        testThetasHomed()
//        testTheta1OnXAxis()
//
//        /*testThetasAlongXAxis()
//         * this likely doesn't pass the assert
//         * because of reach + wrong wrist orientation provided
//        */
//
//        engine.inverseKinematics(
//            getFrameTranslationMatrix(
//                -14.0,
//                8.0,
//                259.0
//            ),
//            DoubleArray(6) { 0.0 },
//            cmmInputArmDhParams
//        )
//
//        val jointAngles = engine.inverseKinematics(
//            cmmInputArmDhParams.toFrameTransformation(),
//            DoubleArray(6) { 0.0 },
//            cmmInputArmDhParams
//        )
//    }
//
//    @Test
//    @Disabled
//    fun `test puma arm`() {
//        val homeTarget = pumaArmDhParams.toFrameTransformation()
//
//        val tempTarget = immutableListOf(
//            DhParam(0, 18.24, 0, -90),
//            DhParam(14.9, 0, 43.2, 0),
//            DhParam(0, 0, 2, 90),
//            DhParam(43.2, 0, 0, -90),
//            DhParam(0, 0, 0, 90),
//            DhParam(5.6, 0, 0, 0)
//        ).toFrameTransformation().toTransformNR()
//
//        val engine = InverseKinematicsEngine.getInstance()
//
//        val jointAngles = engine.inverseKinematics(
//            homeTarget,
//            DoubleArray(6) { 0.0 },
//            pumaArmDhParams
//        )
//
//        println(jointAngles.joinToString())

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
// }
