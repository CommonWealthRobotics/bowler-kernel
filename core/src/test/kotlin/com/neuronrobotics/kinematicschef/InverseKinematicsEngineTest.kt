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

internal class InverseKinematicsEngineTest {

//    @Test
//    fun `test for error when validating euler angles`() {
//        val dhParams = TestUtil.randomDhParamList(6)
//
//        // Use random dh params because we don't care about their values
//        val wrist1 = SphericalWrist(TestUtil.randomDhParamList(3))
//        val wrist2 = SphericalWrist(TestUtil.randomDhParamList(3))
//        val mockChainIdentifier = mock<ChainIdentifier> {
//            on { identifyChain(dhParams) } doReturn
//                immutableListOf<DhChainElement>(wrist1, wrist2)
//        }
//
//        val mockDhClassifier = mock<DhClassifier> {
//            on { deriveEulerAngles(any()) } doReturn Either.left("Invalid.")
//
//            on { deriveEulerAngles(any(), any(), any()) } doReturn Either.left("Invalid.")
//        }
//
//        val engine = InverseKinematicsEngine(mockChainIdentifier, mockDhClassifier)
//
//        assertThrows<NotImplementedError> {
//            engine.inverseKinematics(
//                SimpleMatrix.identity(4),
//                listOf(0.0, 0.0, 0.0).toDoubleArray(),
//                dhParams
//            )
//        }
//    }
}
