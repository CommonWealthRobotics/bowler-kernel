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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultBattery
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktunits.quantities.ampere
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.milliampereHour
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.oz
import org.octogonapus.ktunits.quantities.volt

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class BatteryGeneratorTest {

    private val generator = BatteryGenerator()

    @Test
    fun `test battery`() {
        val battery = DefaultBattery(
            width = 2.6.inch,
            length = 1.35.inch,
            height = 1.05.inch,
            voltage = 11.1.volt,
            current = 25.ampere,
            capacity = 1250.milliampereHour,
            mass = 4.3.oz,
            centerOfMass = CenterOfMass(
                0.inch,
                0.inch,
                0.inch
            ),
            specs = emptyImmutableMap()
        )

        val cad = generator.generateCAD(battery)

        assertAll(
            { assertEquals(battery.width.millimeter / 2, cad.bounds.center.x) },
            { assertEquals(battery.length.millimeter / 2, cad.bounds.center.y) },
            { assertEquals(battery.height.millimeter / 2, cad.bounds.center.z) },
            { assertEquals(battery.width.millimeter, cad.bounds.bounds.x) },
            { assertEquals(battery.length.millimeter, cad.bounds.bounds.y) },
            { assertEquals(battery.height.millimeter, cad.bounds.bounds.z) }
        )
    }
}
