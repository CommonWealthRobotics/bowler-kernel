package com.neuronrobotics.bowlerkernel.kinematics.limb.link.model

import arrow.core.right
import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.kinematics.linkScriptData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LinkScriptDataTest {

    @Test
    fun `test json conversion`() {
        val klaxon = Klaxon()

        val expected = klaxon.linkScriptData()

        val decoded = with(LinkScriptData.encoder()) {
            expected.encode()
        }.decode(LinkScriptData.decoder())

        assertEquals(expected.right(), decoded)
    }
}
