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

package com.neuronrobotics.bowlerkernel.hardware.protocol

import arrow.effects.IO
import com.neuronrobotics.bowlerkernel.deviceserver.getPayload
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableSetOf
import org.octogonapus.ktguava.collections.plus
import org.octogonapus.ktguava.collections.toImmutableList
import org.octogonapus.ktguava.collections.toImmutableSet

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class SimplePacketComsProtocolReadGroupTest {

    private val server = MockDeviceServer()

    private val protocol = SimplePacketComsProtocol(
        server = server,
        resourceIdValidator = DefaultResourceIdValidator()
    )

    private val lineSensor1 = makeReadable(32)

    private val lineSensor2 = makeReadable(33)

    private fun makeReadable(pinNumber: Byte): ResourceId {
        return ResourceId(
            DefaultResourceTypes.AnalogIn,
            DefaultAttachmentPoints.Pin(pinNumber)
        )
    }

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
                val result =
                    it.analogRead(immutableListOf(lineSensor1, lineSensor2)).unsafeRunSync()
                assertIterableEquals(listOf(1.0, 2.0), result)
            } pcSends {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE, byteArrayOf(0, 1, 0, 2))
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
                val result =
                    it.analogRead(immutableListOf(lineSensor2, lineSensor1)).unsafeRunSync()

                // Should get the results back in the order the resource id's were given
                assertIterableEquals(listOf(2.0, 1.0), result)
            } pcSends {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE)
                )
            } deviceResponds {
                // The same payload should be sent regardless of resource id order
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE, byteArrayOf(0, 1, 0, 2))
                )
            }
        }
    }

    @Test
    fun `test reading from a read group without discovery first`() {
        protocolTest(protocol, server) {
            operation {
                // Test a write with too few members
                val result =
                    protocol.analogRead(immutableListOf(lineSensor1, lineSensor2)).attempt()
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
                    protocol.analogRead(immutableListOf(lineSensor1)).attempt().unsafeRunSync()
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
                    protocol.analogRead(immutableListOf(lineSensor1, lineSensor1)).attempt()
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
                val result = it.addReadGroup(immutableSetOf(lineSensor1, lineSensor2)).attempt()
                    .unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE, byteArrayOf(2, 1, 2, 2))
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
    fun `test addReadGroup last group member failure`() {
        protocolTest(protocol, server) {
            operation {
                val result = it.addReadGroup(immutableSetOf(lineSensor1, lineSensor2)).attempt()
                    .unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE, byteArrayOf(2, 1, 2, 2)),
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(3, 1, 0, 0, 0, 2, 3, 1, 32)
                    ),
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(3, 1, 0, 0, 2, 4, 3, 1, 33)
                    )
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_REJECTED_GROUP_FULL)
                    )
                )
            }
        }
    }

    @Test
    fun `test addReadGroup two group member failure`() {
        protocolTest(protocol, server) {
            operation {
                val result = it.addReadGroup(immutableSetOf(lineSensor1, lineSensor2)).attempt()
                    .unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE, byteArrayOf(2, 1, 2, 2)),
                    // This one failing means that lineSensor2 should not be discovered
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(3, 1, 0, 0, 0, 2, 3, 1, 32)
                    )
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_REJECTED_GENERIC)
                    )
                )
            }
        }
    }

    @Test
    fun `test adding more read group members than a payload can fit`() {
        val numGroupMembers = (SimplePacketComsProtocol.PAYLOAD_SIZE + 1).toByte()
        val groupMembers = (1..numGroupMembers).map {
            ResourceId(
                DefaultResourceTypes.DigitalIn,
                DefaultAttachmentPoints.Pin(it.toByte())
            )
        }.toImmutableSet()

        var receiveStart = 0.toByte()
        var receiveEnd = 1.toByte()
        fun makePayload(memberNumber: Byte): ByteArray {
            val payload = getPayload(
                SimplePacketComsProtocol.PAYLOAD_SIZE,
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
                SimplePacketComsProtocol.PAYLOAD_SIZE,
                byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
            )
        }.toImmutableList()

        protocolTest(protocol, server) {
            operation {
                val result = protocol.addReadGroup(groupMembers).attempt().unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf(
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(2, 1, 2, numGroupMembers)
                    )
                ) + sendPayloads
            } deviceResponds {
                receivePayloads
            }
        }
    }

    @Test
    fun `test adding too many read groups`() {
        fun discoverGroupWithId(groupId: Byte) {
            // Different but deterministic pin number
            val pinNumber = (groupId + 1).toByte()

            protocolTest(protocol, server) {
                operation {
                    val result = it.addReadGroup(immutableSetOf(makeReadable(pinNumber))).attempt()
                        .unsafeRunSync()
                    assertTrue(result.isRight())
                } pcSends {
                    immutableListOf(
                        getPayload(
                            SimplePacketComsProtocol.PAYLOAD_SIZE,
                            byteArrayOf(2, groupId, (groupId + 1).toByte(), 1)
                        ),
                        getPayload(
                            SimplePacketComsProtocol.PAYLOAD_SIZE,
                            byteArrayOf(3, groupId, 0, 0, 0, 2, 3, 1, pinNumber)
                        )
                    )
                } deviceResponds {
                    immutableListOf(
                        getPayload(
                            SimplePacketComsProtocol.PAYLOAD_SIZE,
                            byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
                        ),
                        getPayload(
                            SimplePacketComsProtocol.PAYLOAD_SIZE,
                            byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
                        )
                    )
                }
            }
        }

        // These are fine
        (1..254).map {
            discoverGroupWithId(it.toByte())
        }

        // 255 is too big
        val groupId = 255.toByte()
        val pinNumber = (groupId + 1).toByte()
        protocolTest(protocol, server) {
            operation {
                val result = it.addReadGroup(immutableSetOf(makeReadable(pinNumber))).attempt()
                    .unsafeRunSync()
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf()
            } deviceResponds {
                immutableListOf()
            }
        }
    }

    @Test
    fun `test generic read group`() {
        val id1 = ResourceId(
            DefaultResourceTypes.AnalogIn,
            DefaultAttachmentPoints.Pin(7)
        )

        val id2 = ResourceId(
            DefaultResourceTypes.AnalogIn,
            DefaultAttachmentPoints.Pin(8)
        )

        protocolTest(protocol, server) {
            operation {
                val result = it.addReadGroup(immutableSetOf(id1, id2)).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE, byteArrayOf(2, 1, 2, 2)),
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(3, 1, 0, 0, 0, 2, 3, 1, 7)
                    ),
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(3, 1, 0, 0, 2, 4, 3, 1, 8)
                    )
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
                    )
                )
            }
        }

        protocolTest(protocol, server) {
            operation {
                val result = it.genericRead(immutableListOf(id1, id2)).unsafeRunSync()
                assertArrayEquals(byteArrayOf(1, 2), result[0])
                assertArrayEquals(byteArrayOf(3, 4), result[1])
            } pcSends {
                immutableListOf(getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE, byteArrayOf()))
            } deviceResponds {
                immutableListOf(
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(1, 2, 3, 4)
                    )
                )
            }
        }
    }

    private fun setupReadGroup() =
        setupReadGroupImpl { addReadGroup(immutableSetOf(lineSensor1, lineSensor2)) }

    private fun setupReadGroupImpl(operation: SimplePacketComsProtocol.() -> IO<Unit>) {
        protocolTest(protocol, server) {
            operation {
                val result = it.operation().attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.PAYLOAD_SIZE, byteArrayOf(2, 1, 2, 2)),
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(3, 1, 0, 0, 0, 2, 3, 1, 32)
                    ),
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(3, 1, 0, 0, 2, 4, 3, 1, 33)
                    )
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        SimplePacketComsProtocol.PAYLOAD_SIZE,
                        byteArrayOf(SimplePacketComsProtocol.STATUS_ACCEPTED)
                    )
                )
            }
        }
    }
}
