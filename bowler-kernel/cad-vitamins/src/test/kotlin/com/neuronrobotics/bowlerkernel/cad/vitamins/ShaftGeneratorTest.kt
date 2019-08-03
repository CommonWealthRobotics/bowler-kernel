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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultShaft
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktunits.quantities.div
import org.octogonapus.ktunits.quantities.gram
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.minus
import org.octogonapus.ktunits.quantities.plus
import org.octogonapus.ktunits.quantities.times

internal class ShaftGeneratorTest {

    private val generator = com.neuronrobotics.bowlerkernel.cad.vitamins.ShaftGenerator()

    @Test
    fun `test square shaft`() {
        val shaft = DefaultShaft.SquareShaft(
            width = 2.millimeter,
            height = 10.millimeter,
            mass = 0.1.gram,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        )

        val cad = generator.generateCAD(shaft)

        assertAll(
            { assertEquals(0.0, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            { assertEquals(shaft.height.millimeter / 2, cad.bounds.center.z) },
            { assertEquals(shaft.width.millimeter, cad.bounds.bounds.x) },
            { assertEquals(shaft.width.millimeter, cad.bounds.bounds.y) },
            { assertEquals(shaft.height.millimeter, cad.bounds.bounds.z) }
        )
    }

    @Test
    fun `test round shaft`() {
        val shaft = DefaultShaft.RoundShaft(
            diameter = 2.millimeter,
            height = 10.millimeter,
            mass = 0.1.gram,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        )

        val cad = generator.generateCAD(shaft)

        assertAll(
            { assertEquals(0.0, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            { assertEquals(shaft.height.millimeter / 2, cad.bounds.center.z) },
            { assertEquals(shaft.width.millimeter, cad.bounds.bounds.x) },
            { assertEquals(shaft.width.millimeter, cad.bounds.bounds.y) },
            { assertEquals(shaft.height.millimeter, cad.bounds.bounds.z) }
        )
    }

    @Test
    fun `test d shaft`() {
        val shaft = DefaultShaft.DShaft(
            diameter = 5.millimeter,
            flatToOppositeSide = 4.millimeter,
            height = 10.millimeter,
            mass = 0.1.gram,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        )

        val cad = generator.generateCAD(shaft)

        assertAll(
            {
                assertEquals(
                    (shaft.flatToOppositeSide - shaft.diameter).millimeter / 2,
                    cad.bounds.center.x
                )
            },
            { assertEquals(0.0, cad.bounds.center.y) },
            { assertEquals(shaft.height.millimeter / 2, cad.bounds.center.z) },
            { assertEquals(shaft.flatToOppositeSide.millimeter, cad.bounds.bounds.x) },
            { assertEquals(shaft.width.millimeter, cad.bounds.bounds.y) },
            { assertEquals(shaft.height.millimeter, cad.bounds.bounds.z) }
        )
    }

    @Test
    fun `test arm`() {
        val shaft = DefaultShaft.ServoHorn.Arm(
            baseDiameter = 8.millimeter,
            tipDiameter = 3.millimeter,
            baseCenterToTipCenterLength = 22.5.millimeter,
            thickness = 2.millimeter,
            baseColumnThickness = 5.4.millimeter,
            points = 1,
            mass = 0.1.gram,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        )

        val cad = generator.generateCAD(shaft)

        assertAll(
            {
                assertEquals(
                    ((shaft.baseCenterToTipCenterLength + shaft.baseDiameter / 2 +
                        shaft.tipDiameter / 2) / 2 - shaft.baseDiameter / 2).millimeter,
                    cad.bounds.center.x
                )
            },
            { assertEquals(0.0, cad.bounds.center.y) },
            { assertEquals((shaft.baseColumnThickness / 2).millimeter, cad.bounds.center.z) },
            {
                assertEquals(
                    (shaft.baseCenterToTipCenterLength + shaft.baseDiameter / 2 +
                        shaft.tipDiameter / 2).millimeter,
                    cad.bounds.bounds.x
                )
            },
            { assertEquals(shaft.baseDiameter.millimeter, cad.bounds.bounds.y) },
            { assertEquals(shaft.baseColumnThickness.millimeter, cad.bounds.bounds.z) }
        )
    }

    @Test
    fun `test double arm`() {
        val shaft = DefaultShaft.ServoHorn.Arm(
            baseDiameter = 8.millimeter,
            tipDiameter = 3.millimeter,
            baseCenterToTipCenterLength = 22.5.millimeter,
            thickness = 2.millimeter,
            baseColumnThickness = 5.4.millimeter,
            points = 2,
            mass = 0.1.gram,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        )

        val cad = generator.generateCAD(shaft)

        assertAll(
            { assertEquals(0.0, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            { assertEquals((shaft.baseColumnThickness / 2).millimeter, cad.bounds.center.z) },
            {
                assertEquals(
                    (shaft.baseCenterToTipCenterLength * 2 + shaft.tipDiameter).millimeter,
                    cad.bounds.bounds.x
                )
            },
            { assertEquals(shaft.baseDiameter.millimeter, cad.bounds.bounds.y) },
            { assertEquals(shaft.baseColumnThickness.millimeter, cad.bounds.bounds.z) }
        )
    }

    @Test
    fun `test cross arm`() {
        val shaft = DefaultShaft.ServoHorn.Arm(
            baseDiameter = 8.millimeter,
            tipDiameter = 3.millimeter,
            baseCenterToTipCenterLength = 22.5.millimeter,
            thickness = 2.millimeter,
            baseColumnThickness = 5.4.millimeter,
            points = 4,
            mass = 0.1.gram,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        )

        val cad = generator.generateCAD(shaft)

        assertAll(
            { assertEquals(0.0, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            { assertEquals((shaft.baseColumnThickness / 2).millimeter, cad.bounds.center.z) },
            {
                assertEquals(
                    (shaft.baseCenterToTipCenterLength * 2 + shaft.tipDiameter).millimeter,
                    cad.bounds.bounds.x
                )
            },
            {
                assertEquals(
                    (shaft.baseCenterToTipCenterLength * 2 + shaft.tipDiameter).millimeter,
                    cad.bounds.bounds.y
                )
            },
            { assertEquals(shaft.baseColumnThickness.millimeter, cad.bounds.bounds.z) }
        )
    }

    @Test
    fun `test wheel`() {
        val shaft = DefaultShaft.ServoHorn.Wheel(
            diameter = 12.millimeter,
            baseDiameter = 4.millimeter,
            thickness = 2.millimeter,
            baseColumnThickness = 5.4.millimeter,
            mass = 0.1.gram,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        )

        val cad = generator.generateCAD(shaft)

        assertAll(
            { assertEquals(0.0, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            { assertEquals((shaft.baseColumnThickness / 2).millimeter, cad.bounds.center.z) },
            { assertEquals(shaft.diameter.millimeter, cad.bounds.bounds.x) },
            { assertEquals(shaft.diameter.millimeter, cad.bounds.bounds.y) },
            { assertEquals(shaft.baseColumnThickness.millimeter, cad.bounds.bounds.z) }
        )
    }
}
