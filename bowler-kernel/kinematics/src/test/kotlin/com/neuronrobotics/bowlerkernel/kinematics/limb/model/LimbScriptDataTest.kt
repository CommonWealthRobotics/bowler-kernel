package com.neuronrobotics.bowlerkernel.kinematics.limb.model

import arrow.core.right
import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.kinematics.limbScriptData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LimbScriptDataTest {

    @Test
    fun `test json conversion`() {
        val klaxon = Klaxon()

        val expected = klaxon.limbScriptData()

        val decoded = with(LimbScriptData.encoder()) {
            expected.encode()
        }.decode(LimbScriptData.decoder())

        assertEquals(expected.right(), decoded)
    }
}
