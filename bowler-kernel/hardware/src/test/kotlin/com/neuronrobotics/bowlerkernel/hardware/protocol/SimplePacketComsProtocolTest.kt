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

import arrow.core.Option
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.isEmpty
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableSetOf

internal class SimplePacketComsProtocolTest {

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

    private val lineSensor1 = ResourceId(
        DefaultResourceTypes.AnalogIn,
        DefaultAttachmentPoints.Pin(32)
    )

    private val lineSensor2 = ResourceId(
        DefaultResourceTypes.AnalogIn,
        DefaultAttachmentPoints.Pin(33)
    )

    @Test
    fun `test addRead`() {
        // Discover a read group
        protocolTest(protocol, device) {
            operation {
                val result = it.addRead(lineSensor1)
                assertTrue(result.isEmpty())
            } pcSends {
                immutableListOf(
                    getPayload(1, 2, 3, 1, 32)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(1)
                )
            }
        }

        // Do a read
        protocolTest(protocol, device) {
            operation {
                val result = it.analogRead(lineSensor1)
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
    fun `test addReadGroup`() {
        // Discover a read group
        protocolTest(protocol, device) {
            operation {
                val result = it.addReadGroup(immutableSetOf(lineSensor1, lineSensor2))
                assertTrue(result.isEmpty())
            } pcSends {
                immutableListOf(
                    getPayload(2, 1, 2, 2),
                    getPayload(3, 1, 0, 0, 0, 2, 3, 1, 32),
                    getPayload(3, 1, 0, 0, 2, 4, 3, 1, 33)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(1),
                    getPayload(1),
                    getPayload(1)
                )
            }
        }

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

        protocolTest(protocol, device) {
            operation {
                // Test a write with missing members
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
    fun `test addWrite`() {
        // Discover a write group
        protocolTest(protocol, device) {
            operation {
                val result = it.addWrite(led1)
                assertTrue(result.isEmpty())
            } pcSends {
                immutableListOf(
                    getPayload(1, 2, 2, 1, 32)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(1)
                )
            }
        }

        // Do a write
        protocolTest(protocol, device) {
            operation {
                it.digitalWrite(led1, DigitalState.HIGH)
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
    fun `test addWriteGroup`() {
        protocolTest(protocol, device) {
            operation {
                it.addWriteGroup(immutableSetOf(led1, led2))
            } pcSends {
                immutableListOf(
                    getPayload(2, 1, 2, 2),
                    getPayload(3, 1, 0, 1, 0, 0, 2, 1, 32),
                    getPayload(3, 1, 1, 2, 0, 0, 2, 1, 33)
                )
            } deviceResponds {
                immutableListOf(
                    getPayload(1),
                    getPayload(1),
                    getPayload(1)
                )
            }
        }

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
    fun `test resource types are validated in add operations`() {
        assertAll(
            { assertOperationFailedAndNoInteractionsWithDevice { protocol.addRead(led1) } },
            { assertOperationFailedAndNoInteractionsWithDevice { protocol.addPollingRead(led1) } },
            {
                assertOperationFailedAndNoInteractionsWithDevice {
                    protocol.addPollingReadGroup(
                        immutableSetOf(led1, led2)
                    )
                }
            },
            {
                assertOperationFailedAndNoInteractionsWithDevice {
                    protocol.addReadGroup(
                        immutableSetOf(led1, led2)
                    )
                }
            },
            { assertOperationFailedAndNoInteractionsWithDevice { protocol.addWrite(lineSensor1) } },
            {
                assertOperationFailedAndNoInteractionsWithDevice {
                    protocol.addWriteGroup(
                        immutableSetOf(lineSensor1, lineSensor2)
                    )
                }
            }
        )
    }

    @ParameterizedTest
    @MethodSource("isGreaterThanUnsignedByteSource")
    fun `test isGreaterThanUnsignedByte`(data: Pair<Int, Boolean>) {
        assertEquals(
            data.second,
            SimplePacketComsProtocol.isGreaterThanUnsignedByte(data.first)
        )
    }

    @Test
    fun `test starting packet id less than zero`() {
        assertThrows<IllegalArgumentException> {
            SimplePacketComsProtocol(
                comms = device,
                startPacketId = -1,
                resourceIdValidator = DefaultResourceIdValidator()
            )
        }
    }

    @Test
    fun `test starting packet id equal to discovery packet id`() {
        assertThrows<IllegalArgumentException> {
            SimplePacketComsProtocol(
                comms = device,
                startPacketId = SimplePacketComsProtocol.DISCOVERY_PACKET_ID,
                resourceIdValidator = DefaultResourceIdValidator()
            )
        }
    }

    @Test
    fun `test discard success before disconnect`() {
        connectProtocol()

        device.readsToSend.addLast(getPayload(SimplePacketComsProtocol.STATUS_DISCARD_IN_PROGRESS))
        device.readsToSend.addLast(getPayload(SimplePacketComsProtocol.STATUS_DISCARD_IN_PROGRESS))
        device.readsToSend.addLast(getPayload(SimplePacketComsProtocol.STATUS_DISCARD_COMPLETE))

        val result = disconnectProtocol()

        assertAll(
            listOf(
                { assertTrue(result.isEmpty()) }
            ) + device.writesReceived.map {
                {
                    val expected = getPayload(4)
                    assertArrayEquals(
                        expected,
                        it,
                        """
                    |The sent payload:
                    |${it.joinToString()}
                    |should equal the expected payload:
                    |${expected.joinToString()}
                    """.trimMargin()
                    )
                }
            }
        )
    }

    @Test
    fun `test discard failure before disconnect`() {
        connectProtocol()

        device.readsToSend.addLast(getPayload(SimplePacketComsProtocol.STATUS_DISCARD_IN_PROGRESS))
        device.readsToSend.addLast(getPayload(SimplePacketComsProtocol.STATUS_DISCARD_IN_PROGRESS))
        device.readsToSend.addLast(getPayload(SimplePacketComsProtocol.STATUS_REJECTED_GENERIC))

        val result = disconnectProtocol()

        assertTrue(result.nonEmpty())
    }

    /**
     * Connects the protocol and asserts it connected properly because no error was returned.
     */
    private fun connectProtocol() {
        val connection = protocol.connect()
        assertTrue(connection.isEmpty())
    }

    /**
     * Disconnects the protocol.
     */
    private fun disconnectProtocol() = protocol.disconnect()

    /**
     * Connects the protocol, runs the [operation], and asserts that:
     * 1. The operation failed because an error was returned
     * 2. No interactions happened with the device (nothing was written or read)
     *
     * @param operation The operation to perform.
     */
    private fun assertOperationFailedAndNoInteractionsWithDevice(operation: () -> Option<String>) {
        connectProtocol()

        val result = operation()

        assertAll(
            { assertTrue(result.nonEmpty()) },
            { assertNoInteractionsWithDevice() }
        )
    }

    /**
     * Asserts that no interactions have happened with the device (nothing written).
     */
    private fun assertNoInteractionsWithDevice() {
        assertThat(device.writesReceived, isEmpty)
    }

    /**
     * Creates a mock payload. The payload starts with the given [bytes] and is padded with
     * zeroes to a length of [SimplePacketComsProtocol.PAYLOAD_SIZE].
     *
     * @param bytes The first bytes in the payload.
     * @return The payload.
     */
    private fun getPayload(vararg bytes: Byte): ByteArray =
        bytes + (1..(SimplePacketComsProtocol.PAYLOAD_SIZE - bytes.size)).map { 0.toByte() }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun isGreaterThanUnsignedByteSource() = listOf(
            254 to false,
            255 to false,
            256 to true
        )
    }
}
