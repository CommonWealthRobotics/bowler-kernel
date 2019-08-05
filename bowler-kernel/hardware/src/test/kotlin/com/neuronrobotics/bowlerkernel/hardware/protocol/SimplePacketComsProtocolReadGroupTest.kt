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

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableSetOf
import org.octogonapus.ktguava.collections.plus
import org.octogonapus.ktguava.collections.toImmutableList
import org.octogonapus.ktguava.collections.toImmutableSet
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class SimplePacketComsProtocolReadGroupTest {

    private val device = MockDevice()

    private val protocol = SimplePacketComsProtocol(
        comms = device,
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
    fun `test adding a polling read group`() {
        device.pollingPayload = getPayload()
        setupPollingReadGroup()
        device.pollingLatch.await()
    }

    @Test
    fun `test reading from a read group`() {
        setupReadGroup()

        // Do a read
        protocolTest(protocol, device) {
            operation {
                val result = it.analogRead(immutableListOf(lineSensor1, lineSensor2))
                assertIterableEquals(listOf(1.0, 2.0), result)
            } pcSends {
                immutableListOf(
                    getPayload()
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(0, 1, 0, 2)
                )
            }
        }
    }

    @Test
    fun `test reading from a polling read group`() {
        device.pollingPayload = getPayload(0, 1, 0, 2)
        setupPollingReadGroup()
        device.pollingLatch.await()

        // Do a read
        protocolTest(protocol, device) {
            operation {
                val result = it.analogRead(immutableListOf(lineSensor1, lineSensor2))
                assertIterableEquals(listOf(1.0, 2.0), result)
            } pcSends {
                immutableListOf(
                    getPayload()
                )
            } deviceResponds {
                immutableListOf()
            }
        }
    }

    @Test
    fun `test reading from a read group in the opposite order of discovery`() {
        setupReadGroup()

        // Do a read supplying the resource id's in the opposite order
        protocolTest(protocol, device) {
            operation {
                val result = it.analogRead(immutableListOf(lineSensor2, lineSensor1))

                // Should get the results back in the order the resource id's were given
                assertIterableEquals(listOf(2.0, 1.0), result)
            } pcSends {
                immutableListOf(
                    getPayload()
                )
            } deviceResponds {
                // The same payload should be sent regardless of resource id order
                immutableListOf(
                    getPayload(0, 1, 0, 2)
                )
            }
        }
    }

    @Test
    fun `test reading from a read group without discovery first`() {
        protocolTest(protocol, device) {
            operation {
                // Test a write with too few members
                assertThrows<IllegalArgumentException> {
                    protocol.analogRead(immutableListOf(lineSensor1, lineSensor2))
                }
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

        protocolTest(protocol, device) {
            operation {
                // Test a write with too few members
                assertThrows<IllegalArgumentException> {
                    protocol.analogRead(immutableListOf(lineSensor1))
                }
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

        protocolTest(protocol, device) {
            operation {
                // Test a write with too few members
                assertThrows<IllegalArgumentException> {
                    protocol.analogRead(immutableListOf(lineSensor1, lineSensor1))
                }
            } pcSends {
                emptyImmutableList()
            } deviceResponds {
                emptyImmutableList()
            }
        }
    }

    @Test
    fun `test addReadGroup failure`() {
        protocolTest(protocol, device) {
            operation {
                val result = it.addReadGroup(immutableSetOf(lineSensor1, lineSensor2))
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf(
                    getPayload(2, 1, 2, 2)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.STATUS_REJECTED_GENERIC)
                )
            }
        }
    }

    @Test
    fun `test addReadGroup last group member failure`() {
        protocolTest(protocol, device) {
            operation {
                val result = it.addReadGroup(immutableSetOf(lineSensor1, lineSensor2))
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf(
                    getPayload(2, 1, 2, 2),
                    getPayload(3, 1, 0, 0, 0, 2, 3, 1, 32),
                    getPayload(3, 1, 0, 0, 2, 4, 3, 1, 33)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED),
                    getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED),
                    getPayload(SimplePacketComsProtocol.STATUS_REJECTED_GROUP_FULL)
                )
            }
        }
    }

    @Test
    fun `test addReadGroup two group member failure`() {
        protocolTest(protocol, device) {
            operation {
                val result = it.addReadGroup(immutableSetOf(lineSensor1, lineSensor2))
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf(
                    getPayload(2, 1, 2, 2),
                    getPayload(3, 1, 0, 0, 0, 2, 3, 1, 32),
                    // The receive start is 0 and the receive end is 2 (instead of 2 and 4,
                    // respectively) because the first group member was rejected so the payload
                    // send and receive indices were not incremented.
                    getPayload(3, 1, 0, 0, 0, 2, 3, 1, 33)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED),
                    getPayload(SimplePacketComsProtocol.STATUS_REJECTED_INVALID_GROUP_ID),
                    getPayload(SimplePacketComsProtocol.STATUS_REJECTED_INVALID_GROUP_ID)
                )
            }
        }
    }

    @Test
    fun `test adding more read group members than a payload can fit`() {
        val numGroupMembers = 31.toByte()
        val groupMembers = (1..numGroupMembers).map { makeReadable(it.toByte()) }.toImmutableSet()

        var receiveStart = 0.toByte()
        var receiveEnd = 2.toByte()
        fun makePayload(memberNumber: Byte): ByteArray {
            val payload = getPayload(
                3,
                1,
                0,
                0,
                receiveStart,
                receiveEnd,
                3,
                1,
                memberNumber
            )

            receiveStart = (receiveStart + 2).toByte()
            receiveEnd = (receiveEnd + 2).toByte()

            return payload
        }

        val sendPayloads = (1 until numGroupMembers).map { makePayload(it.toByte()) }
        val receivePayloads = (1..numGroupMembers).map {
            getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED)
        }.toImmutableList()

        protocolTest(protocol, device) {
            operation {
                assertThrows<IllegalStateException> {
                    protocol.addReadGroup(groupMembers)
                }
            } pcSends {
                immutableListOf(getPayload(2, 1, 2, numGroupMembers)) + sendPayloads
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

            protocolTest(protocol, device) {
                operation {
                    val result = it.addReadGroup(immutableSetOf(makeReadable(pinNumber)))
                    assertTrue(result.isRight())
                } pcSends {
                    immutableListOf(
                        getPayload(2, groupId, (groupId + 1).toByte(), 1),
                        getPayload(3, groupId, 0, 0, 0, 2, 3, 1, pinNumber)
                    )
                } deviceResponds {
                    immutableListOf(
                        getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED),
                        getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED)
                    )
                }
            }
        }

        (1..254).map {
            discoverGroupWithId(it.toByte())
        }

        assertThrows<IllegalStateException> {
            discoverGroupWithId(255.toByte())
        }
    }

    private fun setupReadGroup() =
        setupReadGroupImpl { addReadGroup(immutableSetOf(lineSensor1, lineSensor2)) }

    private fun setupPollingReadGroup() =
        setupReadGroupImpl { addPollingReadGroup(immutableSetOf(lineSensor1, lineSensor2)) }

    private fun setupReadGroupImpl(operation: SimplePacketComsProtocol.() -> Either<String, Unit>) {
        protocolTest(protocol, device) {
            operation {
                val result = it.operation()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(2, 1, 2, 2),
                    getPayload(3, 1, 0, 0, 0, 2, 3, 1, 32),
                    getPayload(3, 1, 0, 0, 2, 4, 3, 1, 33)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED),
                    getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED),
                    getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED)
                )
            }
        }
    }
}
