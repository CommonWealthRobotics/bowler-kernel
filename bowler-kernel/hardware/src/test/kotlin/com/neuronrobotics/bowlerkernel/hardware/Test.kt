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
package com.neuronrobotics.bowlerkernel.hardware

import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.protocol.SimplePacketComsProtocol
import edu.wpi.SimplePacketComs.device.UdpDevice
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.octogonapus.guavautil.collections.immutableListOf
import java.net.InetAddress

internal class Test {

    @Test
    fun `test esp32`() {
        val testPin = ResourceId(
            DefaultResourceTypes.DigitalIn,
            DefaultAttachmentPoints.Pin(35)
        )

        val rpc = SimplePacketComsProtocol(
            object : UdpDevice(
                InetAddress.getByAddress(
                    listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                )
            ) {
            },
            2,
            immutableListOf(),
            immutableListOf(),
            immutableListOf(testPin)
        )

        rpc.connect().map {
            fail { it }
        }

        for (i in 0 until 10) {
            println(rpc.analogWrite(testPin, i.toShort()))
        }
    }
}
