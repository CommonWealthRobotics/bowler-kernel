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

import com.commonwealthrobotics.bowlerkernel.deviceserver.getPayload
import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.AttachmentPoint
import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultBowlerRPCProtocolWriteTest {

    private val server = MockDeviceServer()
    private val protocol = DefaultBowlerRPCProtocol(server)
    private val led = ResourceId(digitalOut, AttachmentPoint.Pin(32))

    @Test
    fun `test writing`() {
        // Setup the resource
        setupLed()

        // Do a write
        protocolTest(protocol, server) {
            operation {
                val result = it.writeAndRead(led, byteArrayOf(1))
                result.shouldBe(getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE))
            } pcSends {
                listOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(1))
                )
            } deviceResponds {
                listOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE)
                )
            }
        }
    }

    @Test
    fun `test adding a resource with nonzero send and recv lengths`() {
        val resource = ResourceId(servoWithFeedback, AttachmentPoint.Pin(42))

        // Setup the resource
        protocolTest(protocol, server) {
            operation {
                it.add(resource)
            } pcSends {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_DISCOVERY_ID,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            7,
                            1,
                            42
                        )
                    )
                )
            } deviceResponds {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    )
                )
            }
        }

        // Write to it
        protocolTest(protocol, server) {
            operation {
                it.writeAndRead(resource, byteArrayOf(1, 2)).shouldBe(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(8, 9, 10, 11))
                )
            } pcSends {
                listOf(getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(1, 2)))
            } deviceResponds {
                listOf(getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(8, 9, 10, 11)))
            }
        }
    }

    @Test
    fun `test writing without discovery first`() {
        protocolTest(protocol, server) {
            operation {
                // Test a write with missing members
                assertThrows<UnsupportedOperationException> {
                    protocol.writeAndRead(led, byteArrayOf(1))
                }
            } pcSends {
                emptyList()
            } deviceResponds {
                emptyList()
            }
        }
    }

    @Test
    fun `test addWrite failure`() {
        protocolTest(protocol, server) {
            operation {
                shouldThrow<IllegalStateException> { it.add(led) }
            } pcSends {
                listOf(
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
                listOf(
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
                it.add(
                    ResourceId(
                        servo,
                        AttachmentPoint.PwmPin(1, 544, 2400, 16)
                    )
                )
            } pcSends {
                listOf(
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
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    )
                )
            }
        }
    }

    @Test
    fun `writing with a payload that is too big for the resource type is an error`() {
        // Setup the resource
        setupLed()

        // Do a write with a payload that's bigger than the led's payload size
        protocolTest(protocol, server) {
            operation {
                shouldThrow<IllegalArgumentException> {
                    it.writeAndRead(led, byteArrayOf(1, 2, 3, 4, 5, 6, 7))
                }
            } pcSends {
                listOf()
            } deviceResponds {
                listOf()
            }
        }
    }

    private fun setupLed() {
        protocolTest(protocol, server) {
            operation {
                it.add(led)
            } pcSends {
                listOf(
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
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    )
                )
            }
        }
    }
}
