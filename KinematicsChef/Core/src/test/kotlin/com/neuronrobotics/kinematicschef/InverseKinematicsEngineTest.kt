package com.neuronrobotics.kinematicschef

import arrow.core.Either
import com.neuronrobotics.kinematicschef.classifier.ChainIdentifier
import com.neuronrobotics.kinematicschef.classifier.ClassifierError
import com.neuronrobotics.kinematicschef.classifier.DhClassifier
import com.neuronrobotics.kinematicschef.dhparam.DhChainElement
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class InverseKinematicsEngineTest {

    @Test
    fun `test for error when validating euler angles`() {
        val chain = TestUtil.makeMockChain()

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

        val exception = assertThrows<UnsupportedOperationException> {
            engine.inverseKinematics(
                TransformNR(),
                listOf(0.0, 0.0, 0.0).toDoubleArray(),
                chain
            )
        }

        assertEquals(
            """
                |Wrist 1 invalid.
                |Wrist 2 invalid.
            """.trimMargin(),
            exception.message
        )
    }
}
