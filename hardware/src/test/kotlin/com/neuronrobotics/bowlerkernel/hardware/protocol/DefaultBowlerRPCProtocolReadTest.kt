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

import arrow.fx.IO
import com.neuronrobotics.bowlerkernel.deviceserver.getPayload
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.AttachmentPoint
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultBowlerRPCProtocolReadTest {

    private val server = MockDeviceServer()
    private val protocol = DefaultBowlerRPCProtocol(server)
    private val lineSensor = ResourceId(analogOut, AttachmentPoint.Pin(32))

    @Test
    fun `test adding a read`() {
        setupRead(lineSensor)
    }

    @Test
    fun `test reading`() {
        setupRead(lineSensor)

        protocolTest(protocol, server) {
            operation {
                val result = it.read(lineSensor).unsafeRunSync()
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
                    protocol.read(lineSensor).unsafeRunSync()
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
                val result = it.read(lineSensor).attempt().unsafeRunSync()
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
            digitalIn,
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
                            3,
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

        val response = getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(1, 2))
        protocolTest(protocol, server) {
            operation {
                val result = it.read(id).unsafeRunSync()
                assertArrayEquals(response, result)
            } pcSends {
                immutableListOf(getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf()))
            } deviceResponds {
                immutableListOf(response)
            }
        }
    }

    private fun setupRead(resourceId: ResourceId) = setupReadImpl(resourceId) { add(resourceId) }

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
