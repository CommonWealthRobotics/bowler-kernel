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
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.isEmpty
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.octogonapus.ktguava.collections.immutableSetOf

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class SimplePacketComsProtocolTest {

    private val device = MockDevice()

    private val protocol = SimplePacketComsProtocol(
        comms = device,
        resourceIdValidator = DefaultResourceIdValidator()
    )

    @ParameterizedTest
    @MethodSource("resourceTypesAreValidatedSource")
    fun `test resource types are validated in add operations`(
        operation: SimplePacketComsProtocol.() -> Either<String, Unit>
    ) {
        assertOperationFailedAndNoInteractionsWithDevice { protocol.operation() }
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
            listOf { assertTrue(result.isRight()) } +
                device.writesReceived.map {
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

        assertTrue(result.isLeft())
    }

    @Test
    fun `test total discard failure`() {
        connectProtocol()

        device.readsToSend.addLast(getPayload(SimplePacketComsProtocol.STATUS_REJECTED_GENERIC))

        val result = disconnectProtocol()

        assertTrue(result.isLeft())
    }

    @Test
    fun `allowed to disconnect with no connection`() {
        assertTrue(disconnectProtocol().isRight())
    }

    /**
     * Connects the protocol and asserts it connected properly because no error was returned.
     */
    private fun connectProtocol() {
        val connection = protocol.connect()
        assertTrue(connection.isRight())
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
    private fun assertOperationFailedAndNoInteractionsWithDevice(
        operation: () -> Either<String, Unit>
    ) {
        connectProtocol()

        val result = operation()

        assertAll(
            { assertTrue(result.isLeft()) },
            { assertNoInteractionsWithDevice() }
        )
    }

    /**
     * Asserts that no interactions have happened with the device (nothing written).
     */
    private fun assertNoInteractionsWithDevice() {
        assertThat(device.writesReceived, isEmpty)
    }

    companion object {

        private fun getWritable() = ResourceId(
            DefaultResourceTypes.DigitalOut,
            DefaultAttachmentPoints.Pin(Random.nextInt().toByte())
        )

        private fun getReadable() = ResourceId(
            DefaultResourceTypes.AnalogIn,
            DefaultAttachmentPoints.Pin(Random.nextInt().toByte())
        )

        @Suppress("unused")
        @JvmStatic
        fun isGreaterThanUnsignedByteSource() = listOf(
            254 to false,
            255 to false,
            256 to true
        )

        @Suppress("unused")
        @JvmStatic
        fun resourceTypesAreValidatedSource() =
            listOf<SimplePacketComsProtocol.() -> Either<String, Unit>>(
                { addRead(getWritable()) },
                { addPollingRead(getWritable()) },
                { addPollingReadGroup(immutableSetOf(getWritable(), getWritable())) },
                { addReadGroup(immutableSetOf(getWritable(), getWritable())) },
                { addWrite(getReadable()) },
                { addWriteGroup(immutableSetOf(getReadable(), getReadable())) }
            )
    }
}
