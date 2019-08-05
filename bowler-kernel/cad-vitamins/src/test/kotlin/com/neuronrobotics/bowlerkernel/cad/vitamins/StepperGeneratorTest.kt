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

import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Bolt
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.CenterOfMass
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Shaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultBolt
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultStepperMotor
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cylinder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktunits.quantities.ampere
import org.octogonapus.ktunits.quantities.degree
import org.octogonapus.ktunits.quantities.gram
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.minus
import org.octogonapus.ktunits.quantities.nM
import org.octogonapus.ktunits.quantities.plus
import org.octogonapus.ktunits.quantities.volt
import java.util.concurrent.TimeUnit

@Timeout(value = 5, unit = TimeUnit.SECONDS)
internal class StepperGeneratorTest {

    private val tolerance = 1e-8
    private val generator = StepperGenerator(
        boltGenerator = object :
            VitaminCadGenerator<Bolt> {
            override fun generateCAD(vitamin: Bolt): CSG =
                Cylinder(1.5, (vitamin.headHeight + vitamin.bodyHeight).millimeter).toCSG()

            override fun generateKeepaway(vitamin: Bolt): CSG = generateCAD(vitamin)
        },
        shaftGenerator = object :
            VitaminCadGenerator<Shaft> {
            override fun generateCAD(vitamin: Shaft): CSG =
                Cylinder(3.0, vitamin.height.millimeter).toCSG()

            override fun generateKeepaway(vitamin: Shaft): CSG = generateCAD(vitamin)
        }
    )

    private val motor = DefaultStepperMotor(
        width = 40.millimeter,
        height = 37.millimeter,
        boltHoleSpacing = 31.millimeter,
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
        shaft = DefaultShaft.ServoHorn.Arm(
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
        ),
        voltage = 12.volt,
        holdingTorque = 15.8.nM,
        current = 0.31.ampere,
        stepAngle = 1.8.degree,
        mass = 58.gram,
        centerOfMass = CenterOfMass(
            0.inch,
            0.inch,
            0.inch
        ),
        specs = emptyImmutableMap()
    )

    @Test
    fun `test stepper`() {
        val cad = generator.generateCAD(motor)

        assertAll(
            { assertEquals(0.0, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            {
                assertEquals(
                    (motor.shaft.height - motor.height).millimeter / 2,
                    cad.bounds.center.z,
                    tolerance
                )
            },
            { assertEquals(motor.width.millimeter, cad.bounds.bounds.x) },
            { assertEquals(motor.width.millimeter, cad.bounds.bounds.y) },
            {
                assertEquals(
                    (motor.height + motor.shaft.height).millimeter,
                    cad.bounds.bounds.z
                )
            }
        )
    }

    @Test
    fun `test generate bolts`() {
        val diameter = 4
        val height = 20.0
        val cad = generator.generateBolts(motor, diameter.millimeter, height.millimeter)

        assertAll(
            { assertEquals(0.0, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            { assertEquals(height / 2, cad.bounds.center.z) },
            { assertEquals(motor.boltHoleSpacing.millimeter + diameter, cad.bounds.bounds.x) },
            { assertEquals(motor.boltHoleSpacing.millimeter + diameter, cad.bounds.bounds.y) },
            { assertEquals(height, cad.bounds.bounds.z) }
        )
    }
}
