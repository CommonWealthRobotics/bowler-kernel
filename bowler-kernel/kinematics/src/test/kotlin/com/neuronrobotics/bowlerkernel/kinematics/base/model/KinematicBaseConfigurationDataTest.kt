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
package com.neuronrobotics.bowlerkernel.kinematics.base.model

import arrow.core.right
import com.neuronrobotics.bowlerkernel.kinematics.kinematicBaseConfigurationData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class KinematicBaseConfigurationDataTest {

    @Test
    fun `test json`() {
        val expected = kinematicBaseConfigurationData()

        val decoded = with(KinematicBaseConfigurationData.encoder()) {
            expected.encode()
        }.decode(KinematicBaseConfigurationData.decoder())

        assertEquals(expected.right(), decoded)
    }
}
