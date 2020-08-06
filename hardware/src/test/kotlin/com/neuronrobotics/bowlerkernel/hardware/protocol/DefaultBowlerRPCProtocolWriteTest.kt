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
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.AttachmentPoint
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultBowlerRPCProtocolWriteTest {

    private val server = MockDeviceServer()
    private val protocol = DefaultBowlerRPCProtocol(server)
    private val led = ResourceId(digitalOut, AttachmentPoint.Pin(32))

    @Test
    fun `test writing`() {
        setupWrite()

        // Do a write
        protocolTest(protocol, server) {
            operation {
                val result = it.writeAndRead(led, byteArrayOf(1)).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(1))
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE)
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
                    protocol.writeAndRead(led, byteArrayOf(1)).unsafeRunSync()
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
                val result = it.add(led).attempt().unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_DISCOVERY_ID,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            2,
                            1,
                            32
                        )
                    )
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_REJECTED_GENERIC)
                    )
                )
            }
        }
    }

    @Test
    fun `test writing servo with pwm pin`() {
        protocolTest(protocol, server) {
            operation {
                val result = it.add(
                    ResourceId(
                        servo,
                        AttachmentPoint.PwmPin(1, 544, 2400, 16)
                    )
                ).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_DISCOVERY_ID,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
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
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    )
                )
            }
        }
    }

    @Test
    fun `test generic write`() {
        val id = ResourceId(
            digitalOut,
            AttachmentPoint.Pin(7)
        )

        protocolTest(protocol, server) {
            operation {
                val result = it.add(id).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_DISCOVERY_ID,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            2,
                            1,
                            7
                        )
                    )
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    )
                )
            }
        }

        protocolTest(protocol, server) {
            operation {
                val result = it.writeAndRead(
                    id,
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(1))
                ).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(1)))
            } deviceResponds {
                immutableListOf(getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf()))
            }
        }
    }

    private fun setupWrite() {
        protocolTest(protocol, server) {
            operation {
                val result = it.add(led).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_DISCOVERY_ID,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            2,
                            1,
                            32
                        )
                    )
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    )
                )
            }
        }
    }
}
