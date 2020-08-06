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

import com.commonwealthrobotics.bowlerkernel.deviceserver.getPayload
import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.AttachmentPoint
import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.octogonapus.ktguava.collections.emptyImmutableList
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultBowlerRPCProtocolWriteGroupTest {

    private val server = MockDeviceServer()
    private val protocol = DefaultBowlerRPCProtocol(server)

    private val led1 = ResourceId(
        digitalOut,
        AttachmentPoint.Pin(32)
    )

    private val led2 = ResourceId(
        digitalOut,
        AttachmentPoint.Pin(33)
    )

    @Test
    fun `test adding a write group`() {
        setupWriteGroup()
    }

    @Test
    fun `test writing to a write group`() {
        setupWriteGroup()

        // Do a write
        protocolTest(protocol, server) {
            operation {
                val result = it.writeAndRead(
                    listOf(
                        led1 to byteArrayOf(1),
                        led2 to byteArrayOf(0)
                    )
                ).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                listOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(1, 0))
                )
            } deviceResponds {
                listOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE)
                )
            }
        }
    }

    @Test
    fun `test writing to a write group in the opposite order of discovery`() {
        setupWriteGroup()

        // Do a write supplying the resource id's in the opposite order
        protocolTest(protocol, server) {
            operation {
                val result = it.writeAndRead(
                    listOf(
                        led2 to byteArrayOf(0),
                        led1 to byteArrayOf(1)
                    )
                ).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                listOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(1, 0))
                )
            } deviceResponds {
                // The same payload should be sent regardless of resource id order
                listOf(
                    getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE)
                )
            }
        }
    }

    @Test
    fun `test reading from a read group without discovery first`() {
        protocolTest(protocol, server) {
            operation {
                // Test a write with missing members
                val result = protocol.writeAndRead(
                    listOf(
                        led1 to byteArrayOf(1),
                        led2 to byteArrayOf(0)
                    )
                ).attempt().unsafeRunSync()
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
        setupWriteGroup()

        protocolTest(protocol, server) {
            operation {
                // Test a write with too few members
                val result = protocol.writeAndRead(
                    listOf(
                        led1 to byteArrayOf(1)
                    )
                ).attempt().unsafeRunSync()
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
        setupWriteGroup()

        protocolTest(protocol, server) {
            operation {
                // Test a write with missing members
                val result = protocol.writeAndRead(
                    listOf(
                        led1 to byteArrayOf(1),
                        led1 to byteArrayOf(0)
                    )
                ).attempt().unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                emptyImmutableList()
            } deviceResponds {
                emptyImmutableList()
            }
        }
    }

    @Test
    fun `test addGroup failure`() {
        protocolTest(protocol, server) {
            operation {
                val result = it.addGroup(listOf(led1, led2)).attempt().unsafeRunSync()
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
    fun `test addGroup last group member failure`() {
        protocolTest(protocol, server) {
            operation {
                val result = it.addGroup(listOf(led1, led2)).attempt().unsafeRunSync()
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
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_MEMBER_DISCOVERY_ID,
                            1,
                            0,
                            1,
                            0,
                            0,
                            2,
                            1,
                            32
                        )
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_MEMBER_DISCOVERY_ID,
                            1,
                            1,
                            2,
                            0,
                            0,
                            2,
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
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_REJECTED_GROUP_FULL)
                    )
                )
            }
        }
    }

    @Test
    fun `test addGroup two group member failure`() {
        protocolTest(protocol, server) {
            operation {
                val result = it.addGroup(listOf(led1, led2)).attempt().unsafeRunSync()
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
                    // This one failing means that led2 should not be discovered
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_MEMBER_DISCOVERY_ID,
                            1,
                            0,
                            1,
                            0,
                            0,
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
    fun `test generic write group`() {
        val id1 = ResourceId(
            digitalOut,
            AttachmentPoint.Pin(7)
        )

        val id2 = ResourceId(
            digitalOut,
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
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_MEMBER_DISCOVERY_ID,
                            1,
                            0,
                            1,
                            0,
                            0,
                            2,
                            1,
                            7
                        )
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_MEMBER_DISCOVERY_ID,
                            1,
                            1,
                            2,
                            0,
                            0,
                            2,
                            1,
                            8
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

        protocolTest(protocol, server) {
            operation {
                val result = it.writeAndRead(
                    listOf(
                        id1 to byteArrayOf(1),
                        id2 to byteArrayOf(2)
                    )
                ).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(1, 2)
                    )
                )
            } deviceResponds {
                listOf(getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf()))
            }
        }
    }

    @Test
    fun `test generic write group where the written payload is too large`() {
        val id1 = ResourceId(
            digitalOut,
            AttachmentPoint.Pin(7)
        )

        val id2 = ResourceId(
            digitalOut,
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
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_MEMBER_DISCOVERY_ID,
                            1,
                            0,
                            1,
                            0,
                            0,
                            2,
                            1,
                            7
                        )
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_MEMBER_DISCOVERY_ID,
                            1,
                            1,
                            2,
                            0,
                            0,
                            2,
                            1,
                            8
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

        protocolTest(protocol, server) {
            operation {
                val result = it.writeAndRead(
                    listOf(
                        // Both of these should be one element but are purposefully too long
                        id1 to byteArrayOf(1, 3),
                        id2 to byteArrayOf(2, 4)
                    )
                ).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                // A correct implementation will cut off the extra from the payloads and only send
                // `[1, 2]` instead of `[1, 3, 2, 4]`
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(1, 2)
                    )
                )
            } deviceResponds {
                listOf(getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf()))
            }
        }
    }

    private fun setupWriteGroup() {
        protocolTest(protocol, server) {
            operation {
                val result = it.addGroup(listOf(led1, led2)).attempt().unsafeRunSync()
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
                            1,
                            0,
                            0,
                            2,
                            1,
                            32
                        )
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_MEMBER_DISCOVERY_ID,
                            1,
                            1,
                            2,
                            0,
                            0,
                            2,
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
