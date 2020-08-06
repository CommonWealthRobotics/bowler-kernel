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
@file:SuppressWarnings("LongMethod")

package com.commonwealthrobotics.bowlerkernel.hardware.protocol

import arrow.fx.IO
import com.commonwealthrobotics.bowlerkernel.deviceserver.getPayload
import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.AttachmentPoint
import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.plus
import java.lang.UnsupportedOperationException
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultBowlerRPCProtocolReadGroupTest {

    private val server = MockDeviceServer()
    private val protocol = DefaultBowlerRPCProtocol(server)
    private val lineSensor1 = ResourceId(analogIn, AttachmentPoint.Pin(32))
    private val lineSensor2 = ResourceId(analogIn, AttachmentPoint.Pin(33))

    @Test
    fun `test adding a read group`() {
        setupReadGroup()
    }

    @Test
    fun `test reading from a read group`() {
        setupReadGroup()

        // Do a read
        protocolTest(protocol, server) {
            operation {
                val result = it.read(listOf(lineSensor1, lineSensor2)).unsafeRunSync()
                result.shouldContainPayloads(byteArrayOf(0, 1), byteArrayOf(0, 2))
            } pcSends {
                listOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE)
                )
            } deviceResponds {
                listOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(0, 1, 0, 2))
                )
            }
        }
    }

    @Test
    fun `test reading from a read group in the opposite order of discovery`() {
        setupReadGroup()

        // Do a read supplying the resource id's in the opposite order
        protocolTest(protocol, server) {
            operation {
                val result = it.read(listOf(lineSensor2, lineSensor1)).unsafeRunSync()

                // Should get the results back in the order the resource id's were given
                result.shouldContainPayloads(byteArrayOf(0, 2), byteArrayOf(0, 1))
            } pcSends {
                listOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE)
                )
            } deviceResponds {
                // The same payload should be sent regardless of resource id order
                listOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(0, 1, 0, 2))
                )
            }
        }
    }

    @Test
    fun `test reading from a read group without discovery first`() {
        protocolTest(protocol, server) {
            operation {
                // Test a write with too few members
                val result = protocol.read(listOf(lineSensor1, lineSensor2))
                    .attempt()
                    .unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                emptyImmutableList()
            } deviceResponds {
                emptyImmutableList()
            }
        }
    }

    @Test
    fun `test reading from a read group with too few members`() {
        setupReadGroup()

        protocolTest(protocol, server) {
            operation {
                // Test a write with too few members
                val result =
                    protocol.read(listOf(lineSensor1)).attempt().unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                emptyImmutableList()
            } deviceResponds {
                emptyImmutableList()
            }
        }
    }

    @Test
    fun `test reading from a read group without all members`() {
        setupReadGroup()

        protocolTest(protocol, server) {
            operation {
                // Test a write with too few members
                val result =
                    protocol.read(listOf(lineSensor1, lineSensor1)).attempt()
                        .unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                emptyImmutableList()
            } deviceResponds {
                emptyImmutableList()
            }
        }
    }

    @Test
    fun `test addReadGroup failure`() {
        protocolTest(protocol, server) {
            operation {
                val result = it.addGroup(listOf(lineSensor1, lineSensor2)).attempt()
                    .unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_DISCOVERY_ID,
                            1,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            2
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
    fun `test addReadGroup last group member failure`() {
        protocolTest(protocol, server) {
            operation {
                val result = it.addGroup(listOf(lineSensor1, lineSensor2)).attempt()
                    .unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_DISCOVERY_ID,
                            1,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            2
                        )
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(3, 1, 0, 0, 0, 2, 3, 1, 32)
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(3, 1, 0, 0, 2, 4, 3, 1, 33)
                    )
                )
            } deviceResponds {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_REJECTED_GROUP_FULL)
                    )
                )
            }
        }
    }

    @Test
    fun `test addReadGroup two group member failure`() {
        protocolTest(protocol, server) {
            operation {
                val result = it.addGroup(listOf(lineSensor1, lineSensor2)).attempt()
                    .unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_DISCOVERY_ID,
                            1,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            2
                        )
                    ),
                    // This one failing means that lineSensor2 should not be discovered
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(3, 1, 0, 0, 0, 2, 3, 1, 32)
                    )
                )
            } deviceResponds {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_REJECTED_GENERIC)
                    )
                )
            }
        }
    }

    @Test
    fun `test adding more read group members than a payload can fit`() {
        val numGroupMembers = (DefaultBowlerRPCProtocol.PAYLOAD_SIZE + 1).toByte()
        val groupMembers = (1..numGroupMembers).map {
            ResourceId(
                digitalIn,
                AttachmentPoint.Pin(it.toByte())
            )
        }

        var receiveStart = 0.toByte()
        var receiveEnd = 1.toByte()
        fun makePayload(memberNumber: Byte): ByteArray {
            val payload = getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(
                    3,
                    1,
                    0,
                    0,
                    receiveStart,
                    receiveEnd,
                    1,
                    1,
                    memberNumber
                )
            )

            receiveStart = (receiveStart + 1).toByte()
            receiveEnd = (receiveEnd + 1).toByte()

            return payload
        }

        val sendPayloads = (1 until numGroupMembers).map { makePayload(it.toByte()) }
        val receivePayloads = (1 until numGroupMembers).map {
            getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
            )
        }

        protocolTest(protocol, server) {
            operation {
                val result = protocol.addGroup(groupMembers).attempt().unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_DISCOVERY_ID,
                            1,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            numGroupMembers
                        )
                    )
                ) + sendPayloads
            } deviceResponds {
                receivePayloads
            }
        }
    }

    @Test
    fun `test adding too many read groups`() {
        fun discoverGroupWithId(packetId: Byte) {
            // Different but deterministic pin number
            val pinNumber = (packetId + 1).toByte()
            val groupId =
                (packetId - DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID + 1).toByte()

            protocolTest(protocol, server) {
                operation {
                    val result = it.addGroup(
                        listOf(
                            ResourceId(analogIn, AttachmentPoint.Pin(pinNumber))
                        )
                    ).attempt().unsafeRunSync()
                    assertTrue(result.isRight())
                } pcSends {
                    listOf(
                        getPayload(
                            DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                            byteArrayOf(
                                DefaultBowlerRPCProtocol.OPERATION_GROUP_DISCOVERY_ID,
                                groupId,
                                packetId,
                                1
                            )
                        ),
                        getPayload(
                            DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                            byteArrayOf(
                                DefaultBowlerRPCProtocol.OPERATION_GROUP_MEMBER_DISCOVERY_ID,
                                groupId,
                                0,
                                0,
                                0,
                                2,
                                3,
                                1,
                                pinNumber
                            )
                        )
                    )
                } deviceResponds {
                    listOf(
                        getPayload(
                            DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                            byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                        ),
                        getPayload(
                            DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                            byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                        )
                    )
                }
            }
        }

        // These are fine
        (DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID..255).map {
            discoverGroupWithId(it.toByte())
        }

        // Next one is too big
        protocolTest(protocol, server) {
            operation {
                shouldThrow<UnsupportedOperationException> {
                    it.addGroup(listOf(ResourceId(analogIn, AttachmentPoint.Pin(0))))
                        .attempt()
                        .unsafeRunSync()
                }
            } pcSends {
                listOf()
            } deviceResponds {
                listOf()
            }
        }
    }

    @Test
    fun `test generic read group`() {
        val id1 = ResourceId(
            analogIn,
            AttachmentPoint.Pin(7)
        )

        val id2 = ResourceId(
            analogIn,
            AttachmentPoint.Pin(8)
        )

        protocolTest(protocol, server) {
            operation {
                val result = it.addGroup(listOf(id1, id2)).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_DISCOVERY_ID,
                            1,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            2
                        )
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(3, 1, 0, 0, 0, 2, 3, 1, 7)
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(3, 1, 0, 0, 2, 4, 3, 1, 8)
                    )
                )
            } deviceResponds {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    )
                )
            }
        }

        protocolTest(protocol, server) {
            operation {
                val result = it.read(listOf(id1, id2)).unsafeRunSync()
                assertArrayEquals(byteArrayOf(1, 2), result[0])
                assertArrayEquals(byteArrayOf(3, 4), result[1])
            } pcSends {
                listOf(getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf()))
            } deviceResponds {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(1, 2, 3, 4)
                    )
                )
            }
        }
    }

    private fun setupReadGroup() = setupReadGroupImpl { addGroup(listOf(lineSensor1, lineSensor2)) }

    private fun setupReadGroupImpl(operation: DefaultBowlerRPCProtocol.() -> IO<Unit>) {
        protocolTest(protocol, server) {
            operation {
                val result = it.operation().attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_DISCOVERY_ID,
                            1,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            2
                        )
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_MEMBER_DISCOVERY_ID,
                            1,
                            0,
                            0,
                            0,
                            2,
                            3,
                            1,
                            32
                        )
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_MEMBER_DISCOVERY_ID,
                            1,
                            0,
                            0,
                            2,
                            4,
                            3,
                            1,
                            33
                        )
                    )
                )
            } deviceResponds {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    )
                )
            }
        }
    }
}
