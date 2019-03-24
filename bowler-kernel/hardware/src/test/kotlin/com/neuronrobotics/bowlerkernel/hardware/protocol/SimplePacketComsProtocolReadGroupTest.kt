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

import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableSetOf

internal class SimplePacketComsProtocolReadGroupTest {

    private val device = MockDevice()

    private val protocol = SimplePacketComsProtocol(
        comms = device,
        resourceIdValidator = DefaultResourceIdValidator()
    )

    private val lineSensor1 = ResourceId(
        DefaultResourceTypes.AnalogIn,
        DefaultAttachmentPoints.Pin(32)
    )

    private val lineSensor2 = ResourceId(
        DefaultResourceTypes.AnalogIn,
        DefaultAttachmentPoints.Pin(33)
    )

    @Test
    fun `test adding a read group`() {
        setupReadGroup()
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

    private fun setupReadGroup() {
        // Discover a read group
        protocolTest(protocol, device) {
            operation {
                val result = it.addReadGroup(immutableSetOf(lineSensor1, lineSensor2))
                Assertions.assertTrue(result.isRight())
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
