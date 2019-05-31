/*
 * This file is part of bowler-cad.
 *
 * bowler-cad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-cad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-cad.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlercad.vitamin

import com.neuronrobotics.bowlerkernel.vitamins.vitamin.CenterOfMass
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultServo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Shaft
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cylinder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktunits.quantities.degree
import org.octogonapus.ktunits.quantities.div
import org.octogonapus.ktunits.quantities.gram
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.kgFCm
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.minus
import org.octogonapus.ktunits.quantities.plus
import org.octogonapus.ktunits.quantities.second
import org.octogonapus.ktunits.quantities.volt

internal class ServoGeneratorTest {

    private val tolerance = 1e-6
    private val shaftLength = 7.1.millimeter

    private val generator = ServoGenerator(shaftGenerator = object :
        VitaminCadGenerator<Shaft> {
        override fun generateCAD(vitamin: Shaft): CSG {
            return Cylinder(3.0, shaftLength.millimeter).toCSG()
        }
    })

    @Test
    fun `test servo`() {
        val servo = DefaultServo(
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
            stallTorque = 22.8.kgFCm,
            speed = 60.degree / 0.11.second,
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
            mass = 58.gram,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        )

        val cad = generator.generateCAD(servo)

        assertAll(
            { assertEquals(servo.shaftCenterToTopOfBody.millimeter, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            {
                assertEquals(
                    (shaftLength - (servo.height + shaftLength) / 2).millimeter,
                    cad.bounds.center.z,
                    tolerance
                )
            },
            { assertEquals(servo.flangeWidth.millimeter, cad.bounds.bounds.x) },
            { assertEquals(servo.flangeDepth.millimeter, cad.bounds.bounds.y) },
            { assertEquals((shaftLength + servo.height).millimeter, cad.bounds.bounds.z) }
        )
    }
}
