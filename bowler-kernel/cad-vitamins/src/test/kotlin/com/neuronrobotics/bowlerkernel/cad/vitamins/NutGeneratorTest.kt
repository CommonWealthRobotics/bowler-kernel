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
package com.neuronrobotics.bowlerkernel.cad.vitamins

import com.neuronrobotics.bowlerkernel.vitamins.vitamin.CenterOfMass
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultNut
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktunits.quantities.gram
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.millimeter

internal class NutGeneratorTest {

    private val generator = com.neuronrobotics.bowlerkernel.cad.vitamins.NutGenerator()

    @Test
    fun `test nut`() {
        val nut = DefaultNut(
            width = 18.millimeter,
            height = 10.8.millimeter,
            mass = 10.gram,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        )

        val cad = generator.generateCAD(nut)

        assertAll(
            { assertEquals(0.0, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            { assertEquals(nut.height.millimeter / 2, cad.bounds.center.z) },
            {
                // A vertex should fall along x on both sides so this is the circumscribed
                // dimension
                assertEquals(20.78, cad.bounds.bounds.x, 1e-2)
            },
            { assertEquals(nut.width.millimeter, cad.bounds.bounds.y, 1e-2) },
            { assertEquals(nut.height.millimeter, cad.bounds.bounds.z) }
        )
    }
}
