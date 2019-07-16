package com.neuronrobotics.bowlerkernel.kinematics.limb.link.model

import arrow.core.right
import com.neuronrobotics.bowlerkernel.kinematics.linkConfigurationData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LinkConfigurationDataTest {

    @Test
    fun `test json conversion`() {
        val expected = linkConfigurationData()

        val decoded = with(LinkConfigurationData.encoder()) {
            expected.encode()
        }.decode(LinkConfigurationData.decoder())

        assertEquals(expected.right(), decoded)
    }
}
