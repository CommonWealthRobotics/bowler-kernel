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

import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.protocol.SimplePacketComsProtocol
import edu.wpi.SimplePacketComs.device.UdpDevice
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableSetOf
import java.net.InetAddress

internal class TestWithEsp32 {

    @Test
    @Disabled("Needs real hardware")
    fun `test esp32`() {
        val led1 = ResourceId(
            DefaultResourceTypes.DigitalOut,
            DefaultAttachmentPoints.Pin(32)
        )

        val led2 = ResourceId(
            DefaultResourceTypes.DigitalOut,
            DefaultAttachmentPoints.Pin(33)
        )

        val rpc = SimplePacketComsProtocol(
            comms = object :
                UdpDevice(
                    InetAddress.getByAddress(
                        listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                    )
                ) {
            },
            resourceIdValidator = DefaultResourceIdValidator()
        )

        rpc.connect().mapLeft {
            fail { it }
        }

        if (!rpc.isResourceInRange(led1)) {
            fail { "Not in range" }
        }

        if (!rpc.isResourceInRange(led2)) {
            fail { "Not in range" }
        }

        val ledGroup = immutableSetOf(led1, led2)
        rpc.addWriteGroup(ledGroup)

        repeat(2) {
            rpc.digitalWrite(
                immutableListOf(
                    led1 to DigitalState.HIGH,
                    led2 to DigitalState.LOW
                )
            )
            Thread.sleep(500)

            rpc.digitalWrite(
                immutableListOf(
                    led1 to DigitalState.LOW,
                    led2 to DigitalState.HIGH
                )
            )
            Thread.sleep(500)
        }

        println(rpc.disconnect())
    }

    @Test
    @Disabled("Needs real hardware")
    fun `test 17 servos at the same time`() {
        val servos = listOf(
            2, 4, 5, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27
        ).map {
            ResourceId(
                DefaultResourceTypes.Servo,
                DefaultAttachmentPoints.Pin(it.toByte())
            )
        }

        val rpc = SimplePacketComsProtocol(
            comms = object :
                UdpDevice(
                    InetAddress.getByAddress(
                        listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                    )
                ) {
            },
            resourceIdValidator = DefaultResourceIdValidator()
        )

        rpc.connect().mapLeft {
            fail { it }
        }

        servos.forEach {
            rpc.addWrite(it)
            Thread.sleep(100)
        }

        println(rpc.disconnect())
    }

    @Test
    @Disabled("Needs real hardware")
    fun `test 17 servos in a row with a discard in between each`() {
        val servos = listOf(
            2, 4, 5, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27
        ).map {
            ResourceId(
                DefaultResourceTypes.Servo,
                DefaultAttachmentPoints.Pin(it.toByte())
            )
        }

        val rpc = SimplePacketComsProtocol(
            comms = object :
                UdpDevice(
                    InetAddress.getByAddress(
                        listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                    )
                ) {
            },
            resourceIdValidator = DefaultResourceIdValidator()
        )

        servos.forEach {
            rpc.connect().mapLeft {
                fail { it }
            }

            rpc.addWrite(it)

            println(rpc.disconnect())

            Thread.sleep(100)
        }
    }

    @Test
    @Disabled("Needs real hardware")
    fun `test servo with custom duty cycle`() {
        val servo = ResourceId(
            DefaultResourceTypes.Servo,
            DefaultAttachmentPoints.PwmPin(
                pinNumber = 2,
                minUsLow = 400,
                maxUsHigh = 2400,
                timerWidth = 18
            )
        )

        val rpc = SimplePacketComsProtocol(
            comms = object :
                UdpDevice(
                    InetAddress.getByAddress(
                        listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                    )
                ) {
            },
            resourceIdValidator = DefaultResourceIdValidator()
        )

        rpc.connect().mapLeft {
            fail { it }
        }

        rpc.addWrite(servo)

        println(rpc.disconnect())
    }
}
