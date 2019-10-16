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
package com.neuronrobotics.bowlerkernel.hardware.device.deviceid

import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class Esp32WROOM32Test {

    @Test
    fun `test dsl`() {
        deviceTypeTest(DefaultDeviceTypes.Esp32wroom32) {
            digitalOut(2, 4..5, 12..19, 21..23, 25..27, 32..33)
            digitalIn(4, 14, 16..19, 21..23, 25..27, 32..36, 39)
            analogOut(4, 5, 12..19, 21..23, 25..27, 32..33)
            analogIn(4, 14, 16..19, 21..23, 25..27, 32..36, 39)
            serial(1 to 3)
        }
    }
}
