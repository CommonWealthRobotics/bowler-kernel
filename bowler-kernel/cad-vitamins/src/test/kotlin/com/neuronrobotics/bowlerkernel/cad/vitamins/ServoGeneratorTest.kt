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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultServo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultShaft
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktunits.quantities.degree
import org.octogonapus.ktunits.quantities.div
import org.octogonapus.ktunits.quantities.gram
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.kgfCm
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.minus
import org.octogonapus.ktunits.quantities.plus
import org.octogonapus.ktunits.quantities.second
import org.octogonapus.ktunits.quantities.volt
import java.util.concurrent.TimeUnit

@Timeout(value = 15, unit = TimeUnit.SECONDS)
internal class ServoGeneratorTest {

    private val tolerance = 1e-6

    private val generator =
        ServoGenerator(ShaftGenerator())

    private val servo = DefaultServo(
        width = 40.millimeter,
        depth = 20.millimeter,
        height = 37.millimeter,
        shaftCenterToTopOfBody = (40 - 29.9).millimeter,
        flangeWidth = 54.5.millimeter,
        flangeDepth = 20.0.millimeter,
        flangeHeight = 2.5.millimeter,
        flangeHeightFromBottomOfBody = 28.7.millimeter,
        boltWidthSeparation = 49.5.millimeter,
        boltDepthSeparation = 2.millimeter,
        boltHoleDiameter = 5.millimeter,
        voltage = 7.4.volt,
        stallTorque = 22.8.kgfCm,
        speed = 60.degree / 0.11.second,
        shaft = DefaultShaft.RoundShaft(
            diameter = 4.millimeter,
            height = 7.1.millimeter,
            mass = 0.1.gram,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        ),
        mass = 58.gram,
        centerOfMass = CenterOfMass(
            0.inch,
            0.inch,
            0.inch
        ),
        specs = emptyImmutableMap()
    )

    @Test
    fun `test servo`() {
        val cad = generator.generateCAD(servo)

        assertAll(
            { assertEquals(servo.shaftCenterToTopOfBody.millimeter, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            {
                assertEquals(
                    (servo.shaft.height - (servo.height + servo.shaft.height) / 2).millimeter,
                    cad.bounds.center.z,
                    tolerance
                )
            },
            { assertEquals(servo.flangeWidth.millimeter, cad.bounds.bounds.x) },
            { assertEquals(servo.flangeDepth.millimeter, cad.bounds.bounds.y) },
            { assertEquals((servo.shaft.height + servo.height).millimeter, cad.bounds.bounds.z) }
        )
    }

    @Test
    fun `test generate bolts`() {
        val diameter = 4
        val height = 20.0
        val cad = generator.generateBolts(servo, diameter.millimeter, height.millimeter)

        val bodyTopToFlangeTop =
            (servo.height - servo.flangeHeightFromBottomOfBody - servo.flangeHeight).millimeter

        assertAll(
            { assertEquals(servo.shaftCenterToTopOfBody.millimeter, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            {
                assertEquals(
                    (height - bodyTopToFlangeTop * 2) / 2,
                    cad.bounds.center.z,
                    tolerance
                )
            },
            { assertEquals(servo.boltWidthSeparation.millimeter + diameter, cad.bounds.bounds.x) },
            { assertEquals(servo.boltDepthSeparation.millimeter + diameter, cad.bounds.bounds.y) },
            { assertEquals(height, cad.bounds.bounds.z) }
        )
    }
}
