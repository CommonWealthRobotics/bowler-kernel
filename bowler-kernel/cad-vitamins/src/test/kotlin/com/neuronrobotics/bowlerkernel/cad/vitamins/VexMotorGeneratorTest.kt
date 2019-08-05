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

import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexEDRMotor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktunits.quantities.div
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.minus
import org.octogonapus.ktunits.quantities.plus
import java.util.concurrent.TimeUnit

@Timeout(value = 5, unit = TimeUnit.SECONDS)
internal class VexMotorGeneratorTest {

    private val generator = VexMotorGenerator(ShaftGenerator())

    @Test
    fun `test 393 cad`() {
        val motor = VexEDRMotor.VexMotor393
        val cad = generator.generateCAD(motor)

        assertAll(
            { assertEquals((motor.width / 2 - motor.axelInset).millimeter, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            {
                assertEquals(
                    (motor.shaft.height - motor.height).millimeter / 2,
                    cad.bounds.center.z,
                    1e-6
                )
            },
            { assertEquals(motor.width.millimeter, cad.bounds.bounds.x) },
            { assertEquals(motor.depth.millimeter, cad.bounds.bounds.y) },
            { assertEquals((motor.shaft.height + motor.height).millimeter, cad.bounds.bounds.z) }
        )
    }

    @Test
    fun `test 269 cad`() {
        val motor = VexEDRMotor.VexMotor269
        val cad = generator.generateCAD(motor)

        assertAll(
            { assertEquals((motor.width / 2 - motor.axelInset).millimeter, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            {
                assertEquals(
                    (motor.shaft.height - motor.height).millimeter / 2,
                    cad.bounds.center.z,
                    1e-6
                )
            },
            { assertEquals(motor.width.millimeter, cad.bounds.bounds.x) },
            { assertEquals(motor.depth.millimeter, cad.bounds.bounds.y) },
            { assertEquals((motor.shaft.height + motor.height).millimeter, cad.bounds.bounds.z) }
        )
    }
}
