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

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf

internal class SimplePacketComsProtocolReadTest {

    private val device = MockDevice()

    private val protocol = SimplePacketComsProtocol(
        comms = device,
        resourceIdValidator = DefaultResourceIdValidator()
    )

    private val lineSensor = ResourceId(
        DefaultResourceTypes.AnalogIn,
        DefaultAttachmentPoints.Pin(32)
    )

    @Test
    fun `test adding a read`() {
        setupRead()
    }

    @Test
    fun `test adding a polling read`() {
        device.pollingPayload = getPayload()
        setupPollingRead()
        device.pollingLatch.await()
    }

    @Test
    fun `test reading`() {
        setupRead()

        protocolTest(protocol, device) {
            operation {
                val result = it.analogRead(lineSensor)
                assertEquals(1.0, result)
            } pcSends {
                immutableListOf(
                    getPayload()
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(0, 1)
                )
            }
        }
    }

    @Test
    fun `test reading with polling`() {
        device.pollingPayload = getPayload(0, 1)
        setupPollingRead()
        device.pollingLatch.await()

        protocolTest(protocol, device) {
            operation {
                val result = it.analogRead(lineSensor)
                assertEquals(1.0, result)
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
    fun `test reading without discovery first`() {
        protocolTest(protocol, device) {
            operation {
                // Test a write with missing members
                assertThrows<IllegalArgumentException> {
                    protocol.analogRead(lineSensor)
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
        protocolTest(protocol, device) {
            operation {
                val result = it.addRead(lineSensor)
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf(
                    getPayload(1, 2, 3, 1, 32)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.STATUS_REJECTED_GENERIC)
                )
            }
        }
    }

    private fun setupRead() = setupReadImpl { addRead(lineSensor) }

    private fun setupPollingRead() = setupReadImpl { addPollingRead(lineSensor) }

    private fun setupReadImpl(operation: SimplePacketComsProtocol.() -> Either<String, Unit>) {
        protocolTest(protocol, device) {
            operation {
                val result = it.operation()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(1, 2, 3, 1, 32)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED)
                )
            }
        }
    }
}
