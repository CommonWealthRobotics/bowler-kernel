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
@file:SuppressWarnings("LargeClass", "TooManyFunctions", "LongMethod", "LongMethod")

package com.neuronrobotics.bowlerkernel.hardware

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerkernel.deviceserver.DefaultDeviceServer
import com.neuronrobotics.bowlerkernel.deviceserver.UDPTransportLayer
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.protocol.DefaultBowlerRPCProtocol
import java.net.InetAddress
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.octogonapus.ktguava.collections.immutableSetOf

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

        val rpc = DefaultBowlerRPCProtocol(
            server = DefaultDeviceServer(
                UDPTransportLayer(
                    InetAddress.getByAddress(
                        listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                    ),
                    1866
                ),
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE
            ),
            resourceIdValidator = DefaultResourceIdValidator()
        )

        rpc.connect().attempt().unsafeRunSync().mapLeft {
            fail { Throwables.getStackTraceAsString(it) }
        }

        if (!rpc.isResourceInRange(led1).unsafeRunSync()) {
            fail { "Not in range" }
        }

        if (!rpc.isResourceInRange(led2).unsafeRunSync()) {
            fail { "Not in range" }
        }

        val ledGroup = immutableSetOf(led1, led2)
        rpc.addWriteGroup(ledGroup).attempt().unsafeRunSync().mapLeft {
            fail { Throwables.getStackTraceAsString(it) }
        }

        // repeat(2) {
        //     rpc.digitalWrite(
        //         immutableListOf(
        //             led1 to DigitalState.HIGH,
        //             led2 to DigitalState.LOW
        //         )
        //     ).attempt().unsafeRunSync().mapLeft {
        //         fail { Throwables.getStackTraceAsString(it) }
        //     }
        //     Thread.sleep(500)
        //
        //     rpc.digitalWrite(
        //         immutableListOf(
        //             led1 to DigitalState.LOW,
        //             led2 to DigitalState.HIGH
        //         )
        //     ).attempt().unsafeRunSync().mapLeft {
        //         fail { Throwables.getStackTraceAsString(it) }
        //     }
        //     Thread.sleep(500)
        // }

        println(rpc.disconnect().attempt().unsafeRunSync().mapLeft {
            Throwables.getStackTraceAsString(it)
        })
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

        val rpc = DefaultBowlerRPCProtocol(
            server = DefaultDeviceServer(
                UDPTransportLayer(
                    InetAddress.getByAddress(
                        listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                    ),
                    1866
                ),
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE
            ),
            resourceIdValidator = DefaultResourceIdValidator()
        )

        rpc.connect().attempt().unsafeRunSync().mapLeft {
            fail { Throwables.getStackTraceAsString(it) }
        }

        servos.forEach {
            rpc.addWrite(it).attempt().unsafeRunSync().mapLeft {
                fail { Throwables.getStackTraceAsString(it) }
            }
            Thread.sleep(100)
        }

        println(rpc.disconnect().attempt().unsafeRunSync().mapLeft {
            Throwables.getStackTraceAsString(it)
        })
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

        val rpc = DefaultBowlerRPCProtocol(
            server = DefaultDeviceServer(
                UDPTransportLayer(
                    InetAddress.getByAddress(
                        listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                    ),
                    1866
                ),
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE
            ),
            resourceIdValidator = DefaultResourceIdValidator()
        )

        servos.forEach {
            rpc.connect().attempt().unsafeRunSync().mapLeft {
                fail { Throwables.getStackTraceAsString(it) }
            }

            rpc.addWrite(it).attempt().unsafeRunSync().mapLeft {
                fail { Throwables.getStackTraceAsString(it) }
            }

            println(rpc.disconnect().attempt().unsafeRunSync().mapLeft {
                Throwables.getStackTraceAsString(it)
            })

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

        val rpc = DefaultBowlerRPCProtocol(
            server = DefaultDeviceServer(
                UDPTransportLayer(
                    InetAddress.getByAddress(
                        listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                    ),
                    1866
                ),
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE
            ),
            resourceIdValidator = DefaultResourceIdValidator()
        )

        rpc.connect().attempt().unsafeRunSync().mapLeft {
            fail { Throwables.getStackTraceAsString(it) }
        }

        rpc.addWrite(servo).attempt().unsafeRunSync().mapLeft {
            fail { Throwables.getStackTraceAsString(it) }
        }

        println(rpc.disconnect().attempt().unsafeRunSync().mapLeft {
            Throwables.getStackTraceAsString(it)
        })
    }
}
