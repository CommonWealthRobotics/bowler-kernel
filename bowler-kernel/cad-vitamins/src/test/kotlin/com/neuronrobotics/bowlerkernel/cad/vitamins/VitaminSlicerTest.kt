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
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Sphere
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.times

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class VitaminSlicerTest {

    private val slicer = VitaminSlicer()
    private val tolerance = 1e-2

    @Test
    fun `test with sphere`() {
        val expected = CenterOfMass(
            x = 0.millimeter,
            y = 0.millimeter,
            z = 0.millimeter
        )

        val actual = slicer.getCenterOfMass(Sphere(5.0).toCSG())

        checkEquality(expected, actual)
    }

    @Test
    fun `test with cube`() {
        val expected = CenterOfMass(
            x = 0.millimeter,
            y = 0.millimeter,
            z = 0.millimeter
        )

        val actual = slicer.getCenterOfMass(Cube(1.0, 2.0, 3.0).toCSG())

        checkEquality(expected, actual)
    }

    @Test
    fun `test with off-center cube`() {
        val expected = CenterOfMass(
            x = 0.millimeter,
            y = 50.millimeter,
            z = 0.millimeter
        )

        val actual = slicer.getCenterOfMass(Cube(1.0, 2.0, 3.0).toCSG().movey(50))

        checkEquality(expected, actual)
    }

    @Test
    fun `test with two cubes`() {
        val expected = CenterOfMass(
            x = 1.25.millimeter,
            y = 0.millimeter,
            z = 0.millimeter
        )

        val actual = slicer.getCenterOfMass(
            Cube(1.0, 2.0, 3.0).toCSG().union(
                Cube(1.0, 2.0, 3.0).toCSG().movex(2.5)
            )
        )

        checkEquality(expected, actual)
    }

    @Test
    fun `test with two fully separate spheres`() {
        val expected = CenterOfMass(
            x = 0.millimeter,
            y = 0.millimeter,
            z = 2.millimeter
        )

        val actual = slicer.getCenterOfMass(
            Sphere(2.0).toCSG().union(
                Sphere(2.0).toCSG().movez(4)
            )
        )

        checkEquality(expected, actual)
    }

    @Test
    fun `test with centered internal hole`() {
        val expected = CenterOfMass(
            x = 0.millimeter,
            y = 0.millimeter,
            z = 0.millimeter
        )

        val actual = slicer.getCenterOfMass(
            Cube(10.0).toCSG().difference(
                Cube(5.0).toCSG()
            )
        )

        checkEquality(expected, actual)
    }

    @Test
    fun `test with corner removed`() {
        val multiplier = 2
        val expected = CenterOfMass(
            x = (1.25 / 7.0).millimeter * multiplier,
            y = (1.25 / 7.0).millimeter * multiplier,
            z = (1.25 / 7.0).millimeter * multiplier
        )

        val actual = slicer.getCenterOfMass(
            Cube(5.0 * multiplier).toCSG().difference(
                Cube(5.0 * multiplier).toCSG()
                    .movex(-2.5 * multiplier)
                    .movey(-2.5 * multiplier)
                    .movez(-2.5 * multiplier)
            ),
            0.5
        )

        checkEquality(expected, actual, 0.03)
    }

    private fun checkEquality(
        expected: CenterOfMass,
        actual: CenterOfMass,
        customTolerance: Double? = null
    ) {
        val actualTolerance = customTolerance ?: tolerance
        assertTrue(
            expected.approxEquals(actual, actualTolerance, Length::millimeter),
            """
            Expected:
            x=${expected.x.millimeter}, y=${expected.y.millimeter}, z=${expected.z.millimeter}
            Actual:
            x=${actual.x.millimeter}, y=${actual.y.millimeter}, z=${actual.z.millimeter}
            Tolerance:
            $actualTolerance
            """.trimIndent()
        )
    }
}
