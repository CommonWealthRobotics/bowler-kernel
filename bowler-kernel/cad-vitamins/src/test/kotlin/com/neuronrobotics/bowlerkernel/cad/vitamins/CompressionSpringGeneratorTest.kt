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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultCompressionSpring
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktunits.quantities.div
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.kgf
import org.octogonapus.ktunits.quantities.kilogram
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.oz
import java.util.concurrent.TimeUnit

@Timeout(value = 5, unit = TimeUnit.SECONDS)
internal class CompressionSpringGeneratorTest {

    private val generator =
        CompressionSpringGenerator()

    @Test
    fun `test compression spring`() {
        val spring = DefaultCompressionSpring(
            uncompressedHeight = 19.05.millimeter,
            outerDiameter = 15.14.millimeter,
            wireDiameter = 2.millimeter,
            springConstant = 1.39.kgf / 1.millimeter,
            maximumLoad = 12.03.kilogram,
            mass = 4.3.oz,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        )

        val cad = generator.generateCAD(spring)

        assertAll(
            { assertEquals(0.0, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            { assertEquals(spring.uncompressedHeight.millimeter / 2, cad.bounds.center.z) },
            { assertEquals(spring.outerDiameter.millimeter, cad.bounds.bounds.x) },
            { assertEquals(spring.outerDiameter.millimeter, cad.bounds.bounds.y) },
            { assertEquals(spring.uncompressedHeight.millimeter, cad.bounds.bounds.z) }
        )
    }
}
