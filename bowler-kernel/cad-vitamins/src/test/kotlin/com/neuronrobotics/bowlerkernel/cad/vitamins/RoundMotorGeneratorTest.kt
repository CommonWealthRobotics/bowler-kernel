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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultRoundMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultShaft
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktunits.quantities.ampere
import org.octogonapus.ktunits.quantities.degree
import org.octogonapus.ktunits.quantities.div
import org.octogonapus.ktunits.quantities.gram
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.minus
import org.octogonapus.ktunits.quantities.nM
import org.octogonapus.ktunits.quantities.plus
import org.octogonapus.ktunits.quantities.radian
import org.octogonapus.ktunits.quantities.radianPerMinute
import org.octogonapus.ktunits.quantities.times
import org.octogonapus.ktunits.quantities.volt
import org.octogonapus.ktunits.quantities.watt

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class RoundMotorGeneratorTest {

    private val tolerance = 1e-6

    private val generator = RoundMotorGenerator(
        boltGenerator = BoltGenerator(),
        shaftGenerator = ShaftGenerator()
    )

    private val motor = DefaultRoundMotor(
        shaftSupportDiameter = 10.millimeter,
        shaftSupportHeight = 5.8.millimeter,
        gearboxShaftOffset = 7.millimeter,
        gearboxDiameter = 37.millimeter,
        gearboxHeight = 20.millimeter,
        motorDiameter = 34.5.millimeter,
        motorHeight = 30.8.millimeter,
        encoderAssemblyDiameter = 30.millimeter,
        encoderAssemblyHeight = 15.millimeter,
        shaft = DefaultShaft.SquareShaft(
            width = 4.millimeter,
            height = 13.millimeter,
            mass = 0.1.gram,
            specs = emptyImmutableMap()
        ),
        bolt = DefaultBolt(
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
        ),
        boltCircleDiameter = 31.millimeter,
        boltCircleAngleIncrement = 60.degree,
        boltCircleAngleOffset = 30.degree,
        voltage = 0.volt,
        freeSpeed = 0.radianPerMinute,
        freeCurrent = 0.ampere,
        stallTorque = 0.nM,
        stallCurrent = 0.ampere,
        power = 0.watt,
        mass = 0.gram,
        centerOfMass = CenterOfMass(
            0.inch,
            0.inch,
            0.inch
        ),
        specs = emptyImmutableMap()
    )

    @Test
    fun `test round motor`() {
        val cad = generator.generateCAD(motor)

        val totalHeight = motor.shaft.height + motor.shaftSupportHeight + motor.gearboxHeight +
            motor.motorHeight + motor.encoderAssemblyHeight

        val centerHeight = (motor.shaft.height + motor.shaftSupportHeight - (motor.gearboxHeight +
            motor.motorHeight + motor.encoderAssemblyHeight)) / 2

        assertAll(
            { assertEquals(-motor.gearboxShaftOffset.millimeter, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            { assertEquals(centerHeight.millimeter, cad.bounds.center.z) },
            { assertEquals(motor.gearboxDiameter.millimeter, cad.bounds.bounds.x) },
            { assertEquals(motor.gearboxDiameter.millimeter, cad.bounds.bounds.y) },
            { assertEquals(totalHeight.millimeter, cad.bounds.bounds.z) }
        )
    }

    @Test
    fun `test generate bolts`() {
        val diameter = 4.0
        val height = 20.0
        val cad = generator.generateBoltKeepaway(motor, diameter.millimeter, height.millimeter)

        assertAll(
            { assertEquals(-motor.gearboxShaftOffset.millimeter, cad.bounds.center.x, tolerance) },
            { assertEquals(0.0, cad.bounds.center.y) },
            {
                assertEquals(
                    height / 2,
                    cad.bounds.center.z,
                    tolerance
                )
            },
            {
                // Need a bigger tolerance because the vertices distort the measurement
                assertEquals(
                    (motor.boltCircleDiameter *
                        cos(motor.boltCircleAngleOffset.radian)).millimeter + diameter,
                    cad.bounds.bounds.x,
                    1e-1
                )
            },
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
