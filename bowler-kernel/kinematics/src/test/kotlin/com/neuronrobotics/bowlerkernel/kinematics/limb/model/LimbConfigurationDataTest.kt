package com.neuronrobotics.bowlerkernel.kinematics.limb.model

import arrow.core.right
import com.neuronrobotics.bowlerkernel.kinematics.limbConfigurationData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LimbConfigurationDataTest {

    @Test
    fun `test json conversion`() {
        val expected = limbConfigurationData()

        val decoded = with(LimbConfigurationData.encoder()) {
            expected.encode()
        }.decode(LimbConfigurationData.decoder())

        assertEquals(expected.right(), decoded)
    }
}
