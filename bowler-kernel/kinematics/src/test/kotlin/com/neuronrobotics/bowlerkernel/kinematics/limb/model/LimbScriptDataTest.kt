/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
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
