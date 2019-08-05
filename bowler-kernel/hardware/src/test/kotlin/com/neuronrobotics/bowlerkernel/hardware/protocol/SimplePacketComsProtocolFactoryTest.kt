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
package com.neuronrobotics.bowlerkernel.hardware.protocol

import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import java.net.InetAddress
import java.util.concurrent.TimeUnit

@Timeout(value = 15, unit = TimeUnit.SECONDS)
internal class SimplePacketComsProtocolFactoryTest {

    private val factory = SimplePacketComsProtocolFactory(DefaultResourceIdValidator())

    @Test
    fun `test making a protocol with a udp device`() {
        // Just making sure it works
        factory.create(
            DeviceId(
                DefaultDeviceTypes.UnknownDevice,
                DefaultConnectionMethods.InternetAddress(InetAddress.getLoopbackAddress())
            )
        )
    }

    @Test
    fun `test making a protocol with an hid device`() {
        // Just making sure it works
        factory.create(
            DeviceId(
                DefaultDeviceTypes.UnknownDevice,
                DefaultConnectionMethods.RawHID(0, 0)
            )
        )
    }

    @Test
    fun `test making a protocol with an unknown connection type`() {
        assertThrows<UnsupportedOperationException> {
            factory.create(
                DeviceId(
                    mock {},
                    mock {}
                )
            )
        }
    }
}
