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

import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableSetOf

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
