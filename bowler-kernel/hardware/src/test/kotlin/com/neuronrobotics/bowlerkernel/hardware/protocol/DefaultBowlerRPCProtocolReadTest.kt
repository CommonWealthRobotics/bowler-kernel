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

import arrow.effects.IO
import com.neuronrobotics.bowlerkernel.deviceserver.getPayload
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultBowlerRPCProtocolReadTest {

    private val server = MockDeviceServer()

    private val protocol = DefaultBowlerRPCProtocol(
        server = server,
        resourceIdValidator = DefaultResourceIdValidator()
    )

    private val lineSensor = ResourceId(
        DefaultResourceTypes.AnalogIn,
        DefaultAttachmentPoints.Pin(32)
    )

    @Test
    fun `test adding a read`() {
        setupRead(lineSensor)
    }

    @Test
    fun `test reading`() {
        setupRead(lineSensor)

        protocolTest(protocol, server) {
            operation {
                val result = it.analogRead(lineSensor).unsafeRunSync()
                assertEquals(1.0, result)
            } pcSends {
                immutableListOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(0, 1))
                )
            }
        }
    }

    @Test
    fun `test reading without discovery first`() {
        protocolTest(protocol, server) {
            operation {
                // Test a write with missing members
                assertThrows<IllegalArgumentException> {
                    protocol.analogRead(lineSensor).unsafeRunSync()
                }
            } pcSends {
                emptyImmutableList()
            } deviceResponds {
                emptyImmutableList()
            }
        }
    }

    @Test
    fun `test addRead failure`() {
        // Discover a read group
        protocolTest(protocol, server) {
            operation {
                val result = it.addRead(lineSensor).attempt().unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_DISCOVERY_ID,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            3,
                            1,
                            DefaultBowlerRPCProtocol.UNRELIABLE_TRANSPORT,
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
    fun `test generic read`() {
        val id = ResourceId(
            DefaultResourceTypes.AnalogIn,
            DefaultAttachmentPoints.Pin(7)
        )

        protocolTest(protocol, server) {
            operation {
                val result = it.addRead(id).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_DISCOVERY_ID,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            3,
                            1,
                            DefaultBowlerRPCProtocol.UNRELIABLE_TRANSPORT,
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

        val response = getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(1, 2))
        protocolTest(protocol, server) {
            operation {
                val result = it.genericRead(id).unsafeRunSync()
                assertArrayEquals(response, result)
            } pcSends {
                immutableListOf(getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf()))
            } deviceResponds {
                immutableListOf(response)
            }
        }
    }

    @Test
    fun `add resource more than once`() {
        setupRead(lineSensor)

        protocolTest(protocol, server) {
            operation {
                val result = it.addRead(lineSensor).attempt().unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf()
            } deviceResponds {
                immutableListOf()
            }
        }
    }

    @Test
    fun `add a read with reliable transport`() {
        protocolTest(protocol, server) {
            operation {
                val result = it.addRead(lineSensor, true).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_DISCOVERY_ID,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            lineSensor.resourceType.type,
                            lineSensor.attachmentPoint.type,
                            DefaultBowlerRPCProtocol.RELIABLE_TRANSPORT,
                            *lineSensor.attachmentPoint.data
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

    private fun setupRead(resourceId: ResourceId) =
        setupReadImpl(resourceId) { addRead(resourceId) }

    private fun setupReadImpl(
        resourceId: ResourceId,
        operation: DefaultBowlerRPCProtocol.() -> IO<Unit>
    ) {
        protocolTest(protocol, server) {
            operation {
                val result = it.operation().attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_DISCOVERY_ID,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            resourceId.resourceType.type,
                            resourceId.attachmentPoint.type,
                            DefaultBowlerRPCProtocol.UNRELIABLE_TRANSPORT,
                            *resourceId.attachmentPoint.data
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
