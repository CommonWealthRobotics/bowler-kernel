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

import eu.mihosoft.vrl.v3d.CSG
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktunits.quantities.degree
import org.octogonapus.ktunits.quantities.millimeter

internal class GearGeneratorTest {

    private val generator = GearGenerator()

    @Test
    fun `test gear`() {
        val thickness = 6.0
        val gear = generator.makeBevelBox(
            numDriveTeeth = 41,
            numDrivenTeeth = 20,
            thickness = thickness.millimeter,
            toothBaseArchLen = generator.computeGearPitch(26.15.millimeter, 25),
            axelAngleIn = 40.degree,
            helical = 30.millimeter
        )

        val gearA = gear[0] as CSG

        assertAll(
            { assertEquals(0.0, gearA.bounds.center.x, 1e-1) },
            { assertEquals(0.0, gearA.bounds.center.y, 1e-6) },
            { assertEquals(thickness / 2, gearA.bounds.center.z) },
            { assertEquals(thickness, gearA.bounds.bounds.z) }
        )
    }
}
