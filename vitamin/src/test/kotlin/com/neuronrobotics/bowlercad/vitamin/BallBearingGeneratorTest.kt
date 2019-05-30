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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultBallBearing
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.immutableMapOf
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.lbM
import org.octogonapus.ktunits.quantities.millimeter

internal class BallBearingGeneratorTest {

    private val generator = BallBearingGenerator()

    @Test
    fun `test bearing`() {
        val bearing = DefaultBallBearing(
            bore = 12.millimeter,
            width = 8.millimeter,
            diameter = 28.millimeter,
            mass = 0.1.lbM,
            centerOfMass = CenterOfMass(0.inch, 0.inch, 0.inch),
            specs = immutableMapOf(
                "dynamic load capacity" to 1150.lbM,
                "static load capacity" to 540.lbM
            )
        )

        val cad = generator.generateCAD(bearing)

        assertAll(
            { assertEquals(0.0, cad.bounds.center.x) },
            { assertEquals(0.0, cad.bounds.center.y) },
            { assertEquals(bearing.width.millimeter / 2, cad.bounds.center.z) },
            { assertEquals(bearing.diameter.millimeter, cad.bounds.bounds.x) },
            { assertEquals(bearing.diameter.millimeter, cad.bounds.bounds.y) },
            { assertEquals(bearing.width.millimeter, cad.bounds.bounds.z) }
        )
    }
}
