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
@file:SuppressWarnings("LargeClass", "TooManyFunctions", "LongMethod")

package com.neuronrobotics.bowlerkernel.hardware.protocol

import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableSetOf

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class SimplePacketComsProtocolWriteGroupTest {

    private val device = MockDevice()

    private val protocol = SimplePacketComsProtocol(
        comms = device,
        resourceIdValidator = DefaultResourceIdValidator()
    )

    private val led1 = ResourceId(
        DefaultResourceTypes.DigitalOut,
        DefaultAttachmentPoints.Pin(32)
    )

    private val led2 = ResourceId(
        DefaultResourceTypes.DigitalOut,
        DefaultAttachmentPoints.Pin(33)
    )

    @Test
    fun `test adding a write group`() {
        setupWriteGroup()
    }

    @Test
    fun `test writing to a write group`() {
        setupWriteGroup()

        // Do a write
        protocolTest(protocol, device) {
            operation {
                it.digitalWrite(
                    immutableListOf(
                        led1 to DigitalState.HIGH,
                        led2 to DigitalState.LOW
                    )
                )
            } pcSends {
                immutableListOf(
                    getPayload(1, 0)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload()
                )
            }
        }
    }

    @Test
    fun `test writing to a write group in the opposite order of discovery`() {
        setupWriteGroup()

        // Do a write supplying the resource id's in the opposite order
        protocolTest(protocol, device) {
            operation {
                it.digitalWrite(
                    immutableListOf(
                        led2 to DigitalState.LOW,
                        led1 to DigitalState.HIGH
                    )
                )
            } pcSends {
                immutableListOf(
                    getPayload(1, 0)
                )
            } deviceResponds {
                // The same payload should be sent regardless of resource id order
                immutableListOf(
                    getPayload()
                )
            }
        }
    }

    @Test
    fun `test reading from a read group without discovery first`() {
        protocolTest(protocol, device) {
            operation {
                // Test a write with missing members
                assertThrows<IllegalArgumentException> {
                    protocol.digitalWrite(
                        immutableListOf(
                            led1 to DigitalState.HIGH,
                            led2 to DigitalState.LOW
                        )
                    )
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
        setupWriteGroup()

        protocolTest(protocol, device) {
            operation {
                // Test a write with too few members
                assertThrows<IllegalArgumentException> {
                    protocol.digitalWrite(
                        immutableListOf(
                            led1 to DigitalState.HIGH
                        )
                    )
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
        setupWriteGroup()

        protocolTest(protocol, device) {
            operation {
                // Test a write with missing members
                assertThrows<IllegalArgumentException> {
                    protocol.digitalWrite(
                        immutableListOf(
                            led1 to DigitalState.HIGH,
                            led1 to DigitalState.LOW
                        )
                    )
                }
            } pcSends {
                emptyImmutableList()
            } deviceResponds {
                emptyImmutableList()
            }
        }
    }

    @Test
    fun `test addWriteGroup failure`() {
        protocolTest(protocol, device) {
            operation {
                val result = it.addWriteGroup(immutableSetOf(led1, led2))
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
    fun `test addWriteGroup last group member failure`() {
        protocolTest(protocol, device) {
            operation {
                val result = it.addWriteGroup(immutableSetOf(led1, led2))
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf(
                    getPayload(2, 1, 2, 2),
                    getPayload(3, 1, 0, 1, 0, 0, 2, 1, 32),
                    getPayload(3, 1, 1, 2, 0, 0, 2, 1, 33)
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
    fun `test addWriteGroup two group member failure`() {
        protocolTest(protocol, device) {
            operation {
                val result = it.addWriteGroup(immutableSetOf(led1, led2))
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf(
                    getPayload(2, 1, 2, 2),
                    getPayload(3, 1, 0, 1, 0, 0, 2, 1, 32),
                    // The send start is 0 and the send end is 1 (instead of 1 and 2,
                    // respectively) because the first group member was rejected so the payload
                    // send and receive indices were not incremented.
                    getPayload(3, 1, 0, 1, 0, 0, 2, 1, 33)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED),
                    getPayload(SimplePacketComsProtocol.STATUS_REJECTED_GROUP_FULL),
                    getPayload(SimplePacketComsProtocol.STATUS_REJECTED_GROUP_FULL)
                )
            }
        }
    }

    @Test
    fun `test generic write group`() {
        val id1 = ResourceId(
            DefaultResourceTypes.DigitalOut,
            DefaultAttachmentPoints.Pin(7)
        )

        val id2 = ResourceId(
            DefaultResourceTypes.DigitalOut,
            DefaultAttachmentPoints.Pin(8)
        )

        protocolTest(protocol, device) {
            operation {
                val result = it.addWriteGroup(immutableSetOf(id1, id2))
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(2, 1, 2, 2),
                    getPayload(3, 1, 0, 1, 0, 0, 2, 1, 7),
                    getPayload(3, 1, 1, 2, 0, 0, 2, 1, 8)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED)
                )
            }
        }

        protocolTest(protocol, device) {
            operation {
                it.genericWrite(
                    immutableListOf(
                        id1 to byteArrayOf(1),
                        id2 to byteArrayOf(2)
                    )
                )
            } pcSends {
                immutableListOf(getPayload(1, 2))
            } deviceResponds {
                immutableListOf(getPayload())
            }
        }
    }

    @Test
    fun `test generic write group where the written payload is too large`() {
        val id1 = ResourceId(
            DefaultResourceTypes.DigitalOut,
            DefaultAttachmentPoints.Pin(7)
        )

        val id2 = ResourceId(
            DefaultResourceTypes.DigitalOut,
            DefaultAttachmentPoints.Pin(8)
        )

        protocolTest(protocol, device) {
            operation {
                val result = it.addWriteGroup(immutableSetOf(id1, id2))
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(2, 1, 2, 2),
                    getPayload(3, 1, 0, 1, 0, 0, 2, 1, 7),
                    getPayload(3, 1, 1, 2, 0, 0, 2, 1, 8)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED)
                )
            }
        }

        protocolTest(protocol, device) {
            operation {
                it.genericWrite(
                    immutableListOf(
                        // Both of these should be one element but are purposefully too long
                        id1 to byteArrayOf(1, 3),
                        id2 to byteArrayOf(2, 4)
                    )
                )
            } pcSends {
                // A correct implementation will cut off the extra from the payloads and only send
                // `[1, 2]` instead of `[1, 3, 2, 4]`
                immutableListOf(getPayload(1, 2))
            } deviceResponds {
                immutableListOf(getPayload())
            }
        }
    }

    private fun setupWriteGroup() {
        protocolTest(protocol, device) {
            operation {
                val result = it.addWriteGroup(immutableSetOf(led1, led2))
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(2, 1, 2, 2),
                    getPayload(3, 1, 0, 1, 0, 0, 2, 1, 32),
                    getPayload(3, 1, 1, 2, 0, 0, 2, 1, 33)
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
