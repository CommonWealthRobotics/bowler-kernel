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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultBolt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktunits.quantities.gram
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.plus
import java.util.concurrent.TimeUnit

@Timeout(value = 5, unit = TimeUnit.SECONDS)
internal class BoltGeneratorTest {

    private val generator = BoltGenerator()

    @Test
    fun `test bolt with head bigger than body`() {
        val bolt = DefaultBolt(
            headDiameter = 5.35.millimeter,
            headHeight = 2.93.millimeter,
            throughHoleDiameter = 2.93.millimeter,
            bodyHeight = 15.millimeter,
            mass = 10.gram,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        )

        val cad = generator.generateCAD(bolt)

        assertAll(
            { assertEquals(0.0, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            {
                assertEquals(
                    (bolt.headHeight + bolt.bodyHeight).millimeter / 2,
                    cad.bounds.center.z
                )
            },
            { assertEquals(bolt.headDiameter.millimeter, cad.bounds.bounds.x) },
            { assertEquals(bolt.headDiameter.millimeter, cad.bounds.bounds.y) },
            { assertEquals((bolt.headHeight + bolt.bodyHeight).millimeter, cad.bounds.bounds.z) }
        )
    }

    @Test
    fun `test bolt with head smaller than body`() {
        val bolt = DefaultBolt(
            headDiameter = 0.5.millimeter,
            headHeight = 2.93.millimeter,
            throughHoleDiameter = 2.93.millimeter,
            bodyHeight = 15.millimeter,
            mass = 10.gram,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        )

        val cad = generator.generateCAD(bolt)

        assertAll(
            { assertEquals(0.0, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            {
                assertEquals(
                    (bolt.headHeight + bolt.bodyHeight).millimeter / 2,
                    cad.bounds.center.z
                )
            },
            { assertEquals(bolt.throughHoleDiameter.millimeter, cad.bounds.bounds.x) },
            { assertEquals(bolt.throughHoleDiameter.millimeter, cad.bounds.bounds.y) },
            { assertEquals((bolt.headHeight + bolt.bodyHeight).millimeter, cad.bounds.bounds.z) }
        )
    }
}
