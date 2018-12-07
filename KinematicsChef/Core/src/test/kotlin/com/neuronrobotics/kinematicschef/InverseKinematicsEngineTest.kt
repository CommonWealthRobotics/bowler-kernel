/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef

import arrow.core.Either
import com.neuronrobotics.kinematicschef.classifier.ChainIdentifier
import com.neuronrobotics.kinematicschef.classifier.ClassifierError
import com.neuronrobotics.kinematicschef.classifier.DhClassifier
import com.neuronrobotics.kinematicschef.dhparam.DhChainElement
import com.neuronrobotics.kinematicschef.dhparam.RevoluteJoint
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.sdk.addons.kinematics.DHLink
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class InverseKinematicsEngineTest {

    @Test
    fun `test for error when validating euler angles`() {
        val chain = TestUtil.makeMockChain(arrayListOf())

        // Use random dh params because we don't care about their values
        val wrist1 = SphericalWrist(TestUtil.randomDhParamList(3))
        val wrist2 = SphericalWrist(TestUtil.randomDhParamList(3))
        val mockChainIdentifier = mock<ChainIdentifier> {
            on { identifyChain(chain.toDhParams()) } doReturn
                immutableListOf<DhChainElement>(wrist1, wrist2)
        }

        val mockDhClassifier = mock<DhClassifier> {
            on { deriveEulerAngles(wrist1) } doReturn Either.left(
                ClassifierError("Wrist 1 invalid.")
            )
            on { deriveEulerAngles(wrist2) } doReturn Either.left(
                ClassifierError("Wrist 2 invalid.")
            )
        }

        val engine = InverseKinematicsEngine(mockChainIdentifier, mockDhClassifier)

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
    fun `test 6DOF inverse kinematics`() {
        val chain = TestUtil.makeMockChain(
            arrayListOf(
                DHLink(13.0, 180.0, 32.0, -90.0),
                DHLink(25.0, -90.0, 93.0, 180.0),
                DHLink(11.0, 90.0, 24.0, 90.0),
                DHLink(128.0, -90.0, 0.0, 90.0),
                DHLink(0.0, 0.0, 0.0, -90.0),
                DHLink(25.0, 90.0, 0.0, 0.0)
            )
        )

        val dhParams = chain.toDhParams()

        val mockChainIdentifier = mock<ChainIdentifier>() {
            on { identifyChain(dhParams) } doReturn immutableListOf(
                RevoluteJoint(dhParams.subList(0, 1)),
                RevoluteJoint(dhParams.subList(1, 2)),
                RevoluteJoint(dhParams.subList(2, 3)),
                SphericalWrist(dhParams.subList(3, 6))
            )
        }

        val mockDhClassifier = mock<DhClassifier>() {
        }

        val ikEngine = InverseKinematicsEngine(
            mockChainIdentifier,
            mockDhClassifier
        )
    }
}
