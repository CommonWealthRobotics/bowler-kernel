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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultDCMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultShaft
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktunits.quantities.ampere
import org.octogonapus.ktunits.quantities.degree
import org.octogonapus.ktunits.quantities.gram
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.kgfCm
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.minus
import org.octogonapus.ktunits.quantities.plus
import org.octogonapus.ktunits.quantities.radianPerMinute
import org.octogonapus.ktunits.quantities.volt
import org.octogonapus.ktunits.quantities.watt
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DCMotorGeneratorTest {

    private val tolerance = 1e-6

    private val generator = DCMotorGenerator(
        ShaftGenerator(),
        BoltGenerator()
    )

    private val motor = DefaultDCMotor(
        diameter = 25.millimeter,
        height = 30.millimeter,
        shaft = DefaultShaft.DShaft(
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
        ),
        shaftSupportDiameter = 10.millimeter,
        shaftSupportHeight = 5.millimeter,
        bolt = DefaultBolt(
            headDiameter = 2.millimeter,
            headHeight = 0.01.millimeter,
            throughHoleDiameter = 2.millimeter,
            bodyHeight = 5.millimeter,
            mass = 1.gram,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        ),
        boltCircleDiameter = 20.millimeter,
        boltCircleAngleIncrement = 180.degree,
        boltCircleAngleOffset = 90.degree,
        voltage = 7.4.volt,
        freeSpeed = 10.radianPerMinute,
        freeCurrent = 1.ampere,
        stallTorque = 22.8.kgfCm,
        power = 10.watt,
        stallCurrent = 2.ampere,
        mass = 58.gram,
        centerOfMass = CenterOfMass(
            0.inch,
            0.inch,
            0.inch
        ),
        specs = emptyImmutableMap()
    )

    @Test
    fun `test dc motor`() {
        val cad = generator.generateCAD(motor)

        assertAll(
            { assertEquals(0.0, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            {
                assertEquals(
                    (motor.shaft.height - motor.shaftSupportHeight - motor.height).millimeter / 2,
                    cad.bounds.center.z,
                    tolerance
                )
            },
            { assertEquals(motor.diameter.millimeter, cad.bounds.bounds.x) },
            { assertEquals(motor.diameter.millimeter, cad.bounds.bounds.y) },
            {
                assertEquals(
                    (motor.height + motor.shaftSupportHeight + motor.shaft.height).millimeter,
                    cad.bounds.bounds.z
                )
            }
        )
    }

    @Test
    fun `test generate bolts`() {
        val diameter = 4.0
        val height = 20.0
        val cad = generator.generateBoltKeepaway(motor, diameter.millimeter, height.millimeter)

        assertAll(
            { assertEquals(0.0, cad.bounds.center.x, tolerance) },
            { assertEquals(0.0, cad.bounds.center.y) },
            {
                assertEquals(
                    (height - motor.shaftSupportHeight.millimeter * 2) / 2,
                    cad.bounds.center.z,
                    tolerance
                )
            },
            { assertEquals(diameter, cad.bounds.bounds.x, tolerance) },
            { assertEquals(motor.boltCircleDiameter.millimeter + diameter, cad.bounds.bounds.y) },
            {
                assertEquals(
                    height,
                    cad.bounds.bounds.z
                )
            }
        )
    }
}
