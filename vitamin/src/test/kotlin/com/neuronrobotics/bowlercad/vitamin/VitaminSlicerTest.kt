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
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import eu.mihosoft.vrl.v3d.Sphere
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.millimeter

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

        val actual = slicer.getCenterOfMass(Cube(10.0, 20.0, 30.0).toCSG())

        checkEquality(expected, actual)
    }

    @Test
    fun `test with off-center cube`() {
        val expected = CenterOfMass(
            x = 0.millimeter,
            y = 50.millimeter,
            z = 0.millimeter
        )

        val actual = slicer.getCenterOfMass(Cube(10.0, 20.0, 30.0).toCSG().movey(50))

        checkEquality(expected, actual)
    }

    @Test
    fun `test with two cubes`() {
        val expected = CenterOfMass(
            x = 2.5.millimeter,
            y = 0.millimeter,
            z = 0.millimeter
        )

        val actual = slicer.getCenterOfMass(
            Cube(10.0, 20.0, 30.0).toCSG().union(
                Cube(10.0, 20.0, 30.0).toCSG().movex(5)
            )
        )

        checkEquality(expected, actual)
    }

    @Test
    fun `test with two fully separate spheres`() {
        val expected = CenterOfMass(
            x = 0.millimeter,
            y = 0.millimeter,
            z = 10.millimeter
        )

        val actual = slicer.getCenterOfMass(
            Sphere(10.0).toCSG().union(
                Sphere(10.0).toCSG().movez(20)
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
    fun `test with off-center internal hole`() {
        val expected = CenterOfMass(
            x = (12.5 / 3.0).millimeter,
            y = 0.millimeter,
            z = 0.millimeter
        )

        val actual = slicer.getCenterOfMass(
            Cube(50.0).toCSG().difference(
                Cube(50.0).toCSG()
                    .movex(-25)
                    .movez(-25)
            )
        )

        checkEquality(expected, actual)
    }

    @Test
    fun `test with off-center screw hole`() {
        val expected = CenterOfMass(
            x = 0.millimeter,
            y = 0.millimeter,
            z = 0.millimeter
        )

        val actual = slicer.getCenterOfMass(
            Cube(50.0).toCSG().difference(
                Cylinder(5.0, 10.0).toCSG().movex(-5.0)
            )
        )

        checkEquality(expected, actual)
    }

    private fun checkEquality(expected: CenterOfMass, actual: CenterOfMass) {
        assertTrue(
            expected.approxEquals(actual, tolerance, Length::millimeter),
            """
            Expected:
            x=${expected.x.millimeter}, y=${expected.y.millimeter}, z=${expected.z.millimeter}
            Actual:
            x=${actual.x.millimeter}, y=${actual.y.millimeter}, z=${actual.z.millimeter}
            """.trimIndent()
        )
    }
}
