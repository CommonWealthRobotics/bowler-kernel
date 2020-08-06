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

import arrow.fx.IO
import com.commonwealthrobotics.bowlerkernel.deviceserver.getPayload
import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.AttachmentPoint
import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultBowlerRPCProtocolReadTest {

    private val server = MockDeviceServer()
    private val protocol = DefaultBowlerRPCProtocol(server)
    private val lineSensor = ResourceId(analogIn, AttachmentPoint.Pin(32))

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
                result.shouldBe(getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(0, 1)))
            } pcSends {
                listOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE)
                )
            } deviceResponds {
                listOf(
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
                assertThrows<UnsupportedOperationException> {
                    protocol.read(lineSensor).unsafeRunSync()
                }
            } pcSends {
                emptyList()
            } deviceResponds {
                emptyList()
            }
        }
    }

    @Test
    fun `test generic read`() {
        val id = ResourceId(
            analogIn,
            AttachmentPoint.Pin(7)
        )

        protocolTest(protocol, server) {
            operation {
                val result = it.add(id).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                listOf(
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
                listOf(
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
                listOf(getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf()))
            } deviceResponds {
                listOf(response)
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
                listOf(
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
