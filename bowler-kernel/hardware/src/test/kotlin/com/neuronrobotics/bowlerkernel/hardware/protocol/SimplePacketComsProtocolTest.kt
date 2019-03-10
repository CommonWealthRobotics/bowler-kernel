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

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableSetOf

internal class SimplePacketComsProtocolTest {

    private val device = object : AbstractSimpleComsDevice() {

        val writes = mutableMapOf<Int, MutableList<Array<Byte>>>()
        val reads = mutableMapOf<Int, MutableList<Array<Byte>>>()

        override fun write(message: ByteArray?, length: Int, howLongToWaitBeforeTimeout: Int) = 0

        override fun connectDeviceImp() = true

        override fun read(message: ByteArray?, howLongToWaitBeforeTimeout: Int) = 0

        override fun disconnectDeviceImp() = true

        override fun writeBytes(id: Int, values: ByteArray) {
            writes.getOrPut(id) { mutableListOf() }.add(values.toTypedArray().padToLength())
        }

        override fun writeBytes(id: Int, values: Array<Byte>) {
            writes.getOrPut(id) { mutableListOf() }.add(values.padToLength())
        }

        override fun readBytes(id: Int): Array<Byte> {
            return reads.getOrPut(id) {
                mutableListOf(arrayOf<Byte>().padToLength())
            }.last().padToLength()
        }

        override fun readBytes(id: Int, values: ByteArray) {
            reads.getOrPut(id) {
                mutableListOf(arrayOf<Byte>().padToLength())
            }.last().forEachIndexed { index, byte ->
                values[index] = byte
            }
        }

        private fun Array<Byte>.padToLength() =
            this + (1..(60 - size)).map { 0.toByte() }.toTypedArray()
    }

    private val protocol = SimplePacketComsProtocol(device)

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
        device.reads.getOrPut(SimplePacketComsProtocol.DISCOVERY_PACKET_ID) { mutableListOf() }
            .add(getPayload(1))

        val connection = protocol.connect()
        assertTrue(connection.isEmpty())

        val write = protocol.addWrite(led1)
        assertTrue(write.isEmpty())

        assertThat(
            device.writes[SimplePacketComsProtocol.DISCOVERY_PACKET_ID]!!,
            hasSize(equalTo(1))
        )
        assertArrayEquals(
            getPayload(1, 2, 2, 1, 32),
            device.writes[SimplePacketComsProtocol.DISCOVERY_PACKET_ID]!!.last()
        )
    }

    @Test
    fun `test add read with analog in`() {
        device.reads.getOrPut(SimplePacketComsProtocol.DISCOVERY_PACKET_ID) { mutableListOf() }
            .add(getPayload(1))

        val connection = protocol.connect()
        assertTrue(connection.isEmpty())

        val write = protocol.addRead(lineSensor1)
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
    }

    @Test
    fun `test add writeGroup with two leds`() {
        device.reads.getOrPut(SimplePacketComsProtocol.DISCOVERY_PACKET_ID) { mutableListOf() }
            .add(getPayload(1))

        val connection = protocol.connect()
        assertTrue(connection.isEmpty())

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

        protocol.digitalWriteGroup(
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
    }

    @Test
    fun `test add readGroup with two line sensors`() {
        device.reads.getOrPut(SimplePacketComsProtocol.DISCOVERY_PACKET_ID) { mutableListOf() }
            .add(getPayload(1))

        val connection = protocol.connect()
        assertTrue(connection.isEmpty())

        val write = protocol.addReadGroup(immutableSetOf(lineSensor1, lineSensor2))
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
    }

    @ParameterizedTest
    @MethodSource("isGreaterThanUnsignedByteSource")
    fun `test isGreaterThanUnsignedByte`(data: Pair<Int, Boolean>) {
        assertEquals(
            data.second,
            SimplePacketComsProtocol.isGreaterThanUnsignedByte(data.first)
        )
    }

    private fun getPayload(vararg bytes: Byte): Array<Byte> =
        bytes.toTypedArray() + (1..(60 - bytes.size)).map { 0.toByte() }.toTypedArray()

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
