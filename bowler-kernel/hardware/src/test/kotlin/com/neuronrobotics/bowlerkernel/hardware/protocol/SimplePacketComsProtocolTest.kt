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
import com.google.common.collect.ImmutableSet
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.natpryce.hamkrest.isEmpty
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableSetOf

internal class SimplePacketComsProtocolTest {

    private val device = object : AbstractSimpleComsDevice() {

        val writes = mutableMapOf<Int, MutableList<ByteArray>>()
        val reads = mutableMapOf<Int, MutableList<ByteArray>>()

        override fun write(message: ByteArray?, length: Int, howLongToWaitBeforeTimeout: Int) = 0

        override fun connectDeviceImp() = true

        override fun read(message: ByteArray?, howLongToWaitBeforeTimeout: Int) = 0

        override fun disconnectDeviceImp() = true

        override fun writeBytes(id: Int, values: ByteArray) {
            writes.getOrPut(id) { mutableListOf() }.add(values.padToLength())
        }

        override fun writeBytes(id: Int, values: Array<Byte>) = throw NotImplementedError()

        override fun readBytes(id: Int) = throw NotImplementedError()

        override fun readBytes(id: Int, values: ByteArray) {
            reads.getOrPut(id) {
                mutableListOf(ByteArray(60) { 0 })
            }.last().forEachIndexed { index, byte ->
                values[index] = byte
            }
        }

        private fun ByteArray.padToLength() =
            this + (1..(60 - size)).map { 0.toByte() }.toByteArray()
    }

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
    fun `test add write with led`() {
        connectProtocol()

        device.reads.getOrPut(SimplePacketComsProtocol.DISCOVERY_PACKET_ID) { mutableListOf() }
            .add(getPayload(1))

        val write = protocol.addWrite(led1)
        assertTrue(write.isEmpty())

        assertAll(
            {
                assertThat(
                    device.writes[SimplePacketComsProtocol.DISCOVERY_PACKET_ID]!!,
                    hasSize(equalTo(1))
                )
            },
            {
                assertArrayEquals(
                    getPayload(1, 2, 2, 1, 32),
                    device.writes[SimplePacketComsProtocol.DISCOVERY_PACKET_ID]!!.last()
                )
            }
        )

        disconnectProtocol()
    }

    @Test
    fun `test addRead`() {
        testAddReadVariety(SimplePacketComsProtocol::addRead)
    }

    @Test
    fun `test addPollingRead`() {
        testAddReadVariety(SimplePacketComsProtocol::addPollingRead)
    }

    /**
     * Tests adding a single read resource.
     */
    private fun testAddReadVariety(
        addOperation: SimplePacketComsProtocol.(ResourceId) -> Option<String>
    ) {
        connectProtocol()

        device.reads.getOrPut(SimplePacketComsProtocol.DISCOVERY_PACKET_ID) { mutableListOf() }
            .add(getPayload(1))

        val write = protocol.addOperation(lineSensor1)
        assertTrue(write.isEmpty())

        assertAll(
            {
                assertThat(
                    device.writes[SimplePacketComsProtocol.DISCOVERY_PACKET_ID]!!,
                    hasSize(equalTo(1))
                )
            },
            {
                assertArrayEquals(
                    getPayload(1, 2, 3, 1, 32),
                    device.writes[SimplePacketComsProtocol.DISCOVERY_PACKET_ID]!![0]
                )
            }
        )

        disconnectProtocol()
    }

    @Test
    fun `test addReadGroup`() {
        testAddReadGroupVariety(SimplePacketComsProtocol::addReadGroup)
    }

    @Test
    fun `test addPollingReadGroup`() {
        testAddReadGroupVariety(SimplePacketComsProtocol::addPollingReadGroup)
    }

    /**
     * Tests adding a read group.
     */
    private fun testAddReadGroupVariety(
        addOperation: SimplePacketComsProtocol.(ImmutableSet<ResourceId>) -> Option<String>
    ) {
        connectProtocol()

        device.reads.getOrPut(SimplePacketComsProtocol.DISCOVERY_PACKET_ID) { mutableListOf() }
            .add(getPayload(1))

        val write = protocol.addOperation(immutableSetOf(lineSensor1, lineSensor2))
        assertTrue(write.isEmpty())

        assertAll(
            {
                assertThat(
                    device.writes[SimplePacketComsProtocol.DISCOVERY_PACKET_ID]!!,
                    hasSize(equalTo(3))
                )
            },
            {
                assertArrayEquals(
                    getPayload(2, 1, 2, 2),
                    device.writes[SimplePacketComsProtocol.DISCOVERY_PACKET_ID]!![0]
                )
            },
            {
                assertArrayEquals(
                    getPayload(3, 1, 0, 0, 0, 2, 3, 1, 32),
                    device.writes[SimplePacketComsProtocol.DISCOVERY_PACKET_ID]!![1]
                )
            },
            {
                assertArrayEquals(
                    getPayload(3, 1, 0, 0, 2, 4, 3, 1, 33),
                    device.writes[SimplePacketComsProtocol.DISCOVERY_PACKET_ID]!![2]
                )
            }
        )

        // Test a correct read
        device.reads.getOrPut(2) { mutableListOf() }.add(getPayload(0, 1, 0, 2))
        val reads1 = protocol.analogRead(
            immutableListOf(
                lineSensor1,
                lineSensor2
            )
        )

        assertAll(
            {
                assertThat(
                    device.writes[2]!!,
                    hasSize(equalTo(1))
                )
            },
            {
                assertArrayEquals(
                    getPayload(),
                    device.writes[2]!![0]
                )
            },
            {
                assertIterableEquals(
                    listOf(1.0, 2.0),
                    reads1
                )
            }
        )

        /*
        Test a correct read in the opposite order. The RPC payload still needs to be sent and
        replied to in the correct order.
         */
        device.reads.getOrPut(2) { mutableListOf() }.add(getPayload(0, 1, 0, 2))
        val reads2 = protocol.analogRead(
            immutableListOf(
                lineSensor2,
                lineSensor1
            )
        )

        assertAll(
            {
                assertThat(
                    device.writes[2]!!,
                    hasSize(equalTo(2))
                )
            },
            {
                assertArrayEquals(
                    getPayload(),
                    device.writes[2]!![1]
                )
            },
            {
                // Should get the readings back in the order we requested them
                assertIterableEquals(listOf(2.0, 1.0), reads2)
            }
        )

        // Test a read with too few members
        assertThrows<IllegalArgumentException> {
            protocol.analogRead(
                immutableListOf(
                    lineSensor1
                )
            )
        }

        // Test a read with missing members
        assertThrows<IllegalArgumentException> {
            protocol.analogRead(
                immutableListOf(
                    lineSensor1,
                    lineSensor1
                )
            )
        }

        disconnectProtocol()
    }

    @Test
    fun `test add writeGroup with two leds`() {
        connectProtocol()

        device.reads.getOrPut(SimplePacketComsProtocol.DISCOVERY_PACKET_ID) { mutableListOf() }
            .add(getPayload(1))

        val write = protocol.addWriteGroup(immutableSetOf(led1, led2))
        assertTrue(write.isEmpty())

        assertAll(
            {
                assertThat(
                    device.writes[SimplePacketComsProtocol.DISCOVERY_PACKET_ID]!!,
                    hasSize(equalTo(3))
                )
            },
            {
                assertArrayEquals(
                    getPayload(2, 1, 2, 2),
                    device.writes[SimplePacketComsProtocol.DISCOVERY_PACKET_ID]!![0]
                )
            },
            {
                assertArrayEquals(
                    getPayload(3, 1, 0, 1, 0, 0, 2, 1, 32),
                    device.writes[SimplePacketComsProtocol.DISCOVERY_PACKET_ID]!![1]
                )
            },
            {
                assertArrayEquals(
                    getPayload(3, 1, 1, 2, 0, 0, 2, 1, 33),
                    device.writes[SimplePacketComsProtocol.DISCOVERY_PACKET_ID]!![2]
                )
            }
        )

        // Test a correct write
        protocol.digitalWrite(
            immutableListOf(
                led1 to DigitalState.HIGH,
                led2 to DigitalState.LOW
            )
        )

        assertAll(
            {
                assertThat(
                    device.writes[2]!!,
                    hasSize(equalTo(1))
                )
            },
            {
                assertArrayEquals(
                    getPayload(1, 0),
                    device.writes[2]!![0]
                )
            }
        )

        // Test a correct write in the opposite order
        protocol.digitalWrite(
            immutableListOf(
                led2 to DigitalState.LOW,
                led1 to DigitalState.HIGH
            )
        )

        assertAll(
            {
                assertThat(
                    device.writes[2]!!,
                    hasSize(equalTo(2))
                )
            },
            {
                assertArrayEquals(
                    getPayload(1, 0),
                    device.writes[2]!![1]
                )
            }
        )

        // Test a write with too few members
        assertThrows<IllegalArgumentException> {
            protocol.digitalWrite(
                immutableListOf(
                    led1 to DigitalState.HIGH
                )
            )
        }

        // Test a write with missing members
        assertThrows<IllegalArgumentException> {
            protocol.digitalWrite(
                immutableListOf(
                    led1 to DigitalState.HIGH,
                    led1 to DigitalState.LOW
                )
            )
        }
    }

    @Test
    fun `test resource types are validated in add operations`() {
        assertAll(
            { assertInteractionAndNoInteractionsWithDevice { protocol.addRead(led1) } },
            { assertInteractionAndNoInteractionsWithDevice { protocol.addPollingRead(led1) } },
            {
                assertInteractionAndNoInteractionsWithDevice {
                    protocol.addPollingReadGroup(
                        immutableSetOf(led1, led2)
                    )
                }
            },
            {
                assertInteractionAndNoInteractionsWithDevice {
                    protocol.addReadGroup(
                        immutableSetOf(led1, led2)
                    )
                }
            },
            { assertInteractionAndNoInteractionsWithDevice { protocol.addWrite(lineSensor1) } },
            {
                assertInteractionAndNoInteractionsWithDevice {
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
    private fun disconnectProtocol() {
        protocol.disconnect()
    }

    /**
     * Connects the protocol, runs the [interaction], and asserts that:
     * 1. The operation failed because an error was returned
     * 2. No interactions happened with the device (nothing was written or read)
     *
     * @param interaction The interaction to perform.
     */
    private fun assertInteractionAndNoInteractionsWithDevice(interaction: () -> Option<String>) {
        connectProtocol()

        val result = interaction()

        assertAll(
            { assertTrue(result.nonEmpty()) },
            { assertNoInteractionsWithDevice() }
        )
    }

    /**
     * Asserts that no interactions have happened with the device (nothing written or read).
     */
    private fun assertNoInteractionsWithDevice() {
        assertAll(
            { assertThat(device.writes.entries, isEmpty) },
            { assertThat(device.reads.entries, isEmpty) }
        )
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
