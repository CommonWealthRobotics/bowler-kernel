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
package com.commonwealthrobotics.bowlerkernel.hardware.protocol

import com.commonwealthrobotics.bowlerkernel.hardware.device.deviceid.ConnectionMethod
import com.commonwealthrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.commonwealthrobotics.bowlerkernel.hardware.device.deviceid.UnknownDeviceType
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultBowlerRPCProtocolFactoryTest {

    private val factory = DefaultBowlerRPCProtocolFactory()

    @Test
    fun `test making a protocol with a udp device`() {
        // Just making sure it works
        factory.create(
            DeviceId(
                UnknownDeviceType,
                ConnectionMethod.InternetAddress(mockk())
            )
        )
    }
}
