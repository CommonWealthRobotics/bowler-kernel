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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class Teensy35Test {

    @Test
    fun `test dsl`() {
        deviceTypeTest(DefaultDeviceTypes.Teensy35) {
            digitalOut(0..57)
            digitalIn(0..57)
            analogOut(2..10, 14, 20..23, 29..30, 35..38)
            analogIn(0..26)
            serial(1 to 0, 10 to 9, 8 to 7, 32 to 31, 33 to 34, 48 to 47)
        }
    }
}
