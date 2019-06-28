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

internal class SimplePacketComsProtocolWriteTest {

    private val device = MockDevice()

    private val protocol = SimplePacketComsProtocol(
        comms = device,
        resourceIdValidator = DefaultResourceIdValidator()
    )

    private val led = ResourceId(
        DefaultResourceTypes.DigitalOut,
        DefaultAttachmentPoints.Pin(32)
    )

    @Test
    fun `test writing`() {
        setupWrite()

        // Do a write
        protocolTest(protocol, device) {
            operation {
                it.digitalWrite(led, DigitalState.HIGH)
            } pcSends {
                immutableListOf(
                    getPayload(1)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload()
                )
            }
        }
    }

    @Test
    fun `test writing without discovery first`() {
        protocolTest(protocol, device) {
            operation {
                // Test a write with missing members
                assertThrows<IllegalArgumentException> {
                    protocol.digitalWrite(led, DigitalState.HIGH)
                }
            } pcSends {
                emptyImmutableList()
            } deviceResponds {
                emptyImmutableList()
            }
        }
    }

    @Test
    fun `test addWrite failure`() {
        protocolTest(protocol, device) {
            operation {
                val result = it.addWrite(led)
                assertTrue(result.isLeft())
            } pcSends {
                immutableListOf(
                    getPayload(1, 2, 2, 1, 32)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.STATUS_REJECTED_GENERIC)
                )
            }
        }
    }

    @Test
    fun `test writing servo with pwm pin`() {
        protocolTest(protocol, device) {
            operation {
                val result = it.addWrite(
                    ResourceId(
                        DefaultResourceTypes.Servo,
                        DefaultAttachmentPoints.PwmPin(1, 544, 2400)
                    )
                )
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(1, 2, 6, 4, 1, 2, 32, 9, 96)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED)
                )
            }
        }
    }

    private fun setupWrite() {
        protocolTest(protocol, device) {
            operation {
                val result = it.addWrite(led)
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(1, 2, 2, 1, 32)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(SimplePacketComsProtocol.STATUS_ACCEPTED)
                )
            }
        }
    }
}
