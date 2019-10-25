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

import com.neuronrobotics.bowlerkernel.deviceserver.getPayload
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class SimplePacketComsProtocolWriteTest {

    private val server = MockDeviceServer()

    private val protocol = SimplePacketComsProtocol(
        server = server,
        resourceIdValidator = DefaultResourceIdValidator()
    )

    private val led = ResourceId(
        DefaultResourceTypes.DigitalOut,
        DefaultAttachmentPoints.Pin(32)
    )

    @Test
    fun `test writing`() {
        setupWrite()

        // Do a write
        protocolTest(protocol, server) {
            operation {
                val result = it.digitalWrite(led, DigitalState.HIGH).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE, byteArrayOf(1))
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE)
                )
            }
        }
    }

    @Test
    fun `test writing without discovery first`() {
        protocolTest(protocol, server) {
            operation {
                // Test a write with missing members
                assertThrows<IllegalArgumentException> {
                    protocol.digitalWrite(led, DigitalState.HIGH).unsafeRunSync()
                }
            } pcSends {
                emptyImmutableList()
            } deviceResponds {
                emptyImmutableList()
            }
        }
    }

    @Test
    fun `test addWrite failure`() {
        protocolTest(protocol, server) {
            operation {
                val result = it.addWrite(led).attempt().unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf(
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            SimplePacketComsProtocol.OPERATION_DISCOVERY_ID,
                            SimplePacketComsProtocol.DEFAULT_START_PACKET_ID,
                            2,
                            1,
                            32
                        )
                    )
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_REJECTED_GENERIC)
                    )
                )
            }
        }
    }

    @Test
    fun `test writing servo with pwm pin`() {
        protocolTest(protocol, server) {
            operation {
                val result = it.addWrite(
                    ResourceId(
                        DefaultResourceTypes.Servo,
                        DefaultAttachmentPoints.PwmPin(1, 544, 2400, 16)
                    )
                ).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            SimplePacketComsProtocol.OPERATION_DISCOVERY_ID,
                            SimplePacketComsProtocol.DEFAULT_START_PACKET_ID,
                            6,
                            4,
                            1,
                            2,
                            32,
                            9,
                            96,
                            16
                        )
                    )
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
                    )
                )
            }
        }
    }

    @Test
    fun `test generic write`() {
        val id = ResourceId(
            DefaultResourceTypes.DigitalOut,
            DefaultAttachmentPoints.Pin(7)
        )

        protocolTest(protocol, server) {
            operation {
                val result = it.addWrite(id).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            SimplePacketComsProtocol.OPERATION_DISCOVERY_ID,
                            SimplePacketComsProtocol.DEFAULT_START_PACKET_ID,
                            2,
                            1,
                            7
                        )
                    )
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
                    )
                )
            }
        }

        protocolTest(protocol, server) {
            operation {
                val result = it.genericWrite(
                    id,
                    getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE, byteArrayOf(1))
                ).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE, byteArrayOf(1)))
            } deviceResponds {
                immutableListOf(getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE, byteArrayOf()))
            }
        }
    }

    private fun setupWrite() {
        protocolTest(protocol, server) {
            operation {
                val result = it.addWrite(led).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            SimplePacketComsProtocol.OPERATION_DISCOVERY_ID,
                            SimplePacketComsProtocol.DEFAULT_START_PACKET_ID,
                            2,
                            1,
                            32
                        )
                    )
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
                    )
                )
            }
        }
    }
}
