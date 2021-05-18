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
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
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
                it.writeAndRead(
                    listOf(
                        led1 to byteArrayOf(1),
                        led2 to byteArrayOf(0)
                    )
                ).shouldContainPayloads(byteArrayOf(), byteArrayOf())
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
                it.writeAndRead(
                    listOf(
                        led2 to byteArrayOf(0),
                        led1 to byteArrayOf(1)
                    )
                ).shouldContainPayloads(byteArrayOf(), byteArrayOf())
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
    fun `reading from a read group without discovery first is an error`() {
        protocolTest(protocol, server) {
            operation {
                // Test a write with missing members
                shouldThrow<IllegalArgumentException> {
                    protocol.writeAndRead(
                        listOf(
                            led1 to byteArrayOf(1),
                            led2 to byteArrayOf(0)
                        )
                    )
                }
            } pcSends {
                emptyList()
            } deviceResponds {
                emptyList()
            }
        }
    }

    @Test
    fun `reading from a read group with too few members is an error`() {
        setupWriteGroup()

        protocolTest(protocol, server) {
            operation {
                // Test a write with too few members
                shouldThrow<IllegalArgumentException> {
                    protocol.writeAndRead(
                        listOf(
                            led1 to byteArrayOf(1)
                        )
                    )
                }
            } pcSends {
                emptyList()
            } deviceResponds {
                emptyList()
            }
        }
    }

    @Test
    fun `reading from a read group without all members is an error`() {
        setupWriteGroup()

        protocolTest(protocol, server) {
            operation {
                // Test a write with missing members. Supply led1 twice to have the correct number of members while
                // missing led2.
                shouldThrow<IllegalArgumentException> {
                    protocol.writeAndRead(
                        listOf(
                            led1 to byteArrayOf(1),
                            led1 to byteArrayOf(0)
                        )
                    )
                }
            } pcSends {
                emptyList()
            } deviceResponds {
                emptyList()
            }
        }
    }

    @Test
    fun `test addGroup failure`() {
        protocolTest(protocol, server) {
            operation {
                shouldThrow<IllegalStateException> {
                    it.addGroup(listOf(led1, led2))
                }
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
                // Have the device response with rejected to test that the group discovery should fail
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
    fun `adding a resource to a full group is an error`() {
        protocolTest(protocol, server) {
            operation {
                shouldThrow<IllegalStateException> {
                    it.addGroup(listOf(led1, led2))
                }
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
                    // Have the device response with rejected to test that the group discovery should fail
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
                shouldThrow<IllegalStateException> {
                    it.addGroup(listOf(led1, led2))
                }
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
                    // Have the device response with rejected to test that the group discovery should fail
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
                it.addGroup(listOf(id1, id2))
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
                it.writeAndRead(
                    listOf(
                        id1 to byteArrayOf(1),
                        id2 to byteArrayOf(2)
                    )
                ).shouldContainPayloads(byteArrayOf(), byteArrayOf())
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
    fun `the payload must be the correct size for each group member`() {
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
                it.addGroup(listOf(id1, id2))
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
                // Both of these should be one element but are purposefully the wrong size
                shouldThrow<IllegalArgumentException> {
                    it.writeAndRead(
                        listOf(
                            id1 to byteArrayOf(),
                            id2 to byteArrayOf(2, 4)
                        )
                    )
                }
            } pcSends {
                // A correct implementation will throw when encountering payloads that are too big
                // for the resources they correspond to. The previous implementation truncated the
                // payloads (e.g. `[1, 3, 2, 4]` became `[1, 2]`).
                listOf()
            } deviceResponds {
                listOf()
            }
        }
    }

    @Test
    fun `writing to two resources in separate groups in one rpc call is not allowed`() {
        // Add led1 to group 1
        protocolTest(protocol, server) {
            operation {
                it.addGroup(listOf(led1))
            } pcSends {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_DISCOVERY_ID,
                            1,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            1
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

        // Add led2 to group 2
        protocolTest(protocol, server) {
            operation {
                it.addGroup(listOf(led2))
            } pcSends {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_DISCOVERY_ID,
                            2,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID.inc(),
                            1
                        )
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_MEMBER_DISCOVERY_ID,
                            2,
                            0,
                            1,
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
                    )
                )
            }
        }

        // Make a group write with led1 and led2
        protocolTest(protocol, server) {
            operation {
                // This should fail because led1 and led2 are in separate groups
                shouldThrow<IllegalArgumentException> {
                    it.writeAndRead(
                        listOf(
                            led1 to byteArrayOf(1),
                            led2 to byteArrayOf(1)
                        )
                    )
                }
            } pcSends {
                listOf()
            } deviceResponds {
                listOf()
            }
        }
    }

    private fun setupWriteGroup() {
        protocolTest(protocol, server) {
            operation {
                it.addGroup(listOf(led1, led2))
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
