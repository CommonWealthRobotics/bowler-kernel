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

import arrow.fx.IO
import com.neuronrobotics.bowlerkernel.deviceserver.getPayload
import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultBowlerRPCProtocolTest {

    private val server = MockDeviceServer()
    private val protocol = DefaultBowlerRPCProtocol(server)

    @ParameterizedTest
    @MethodSource("isGreaterThanUnsignedByteSource")
    fun `test isGreaterThanUnsignedByte`(data: Pair<Int, Boolean>) {
        assertEquals(
            data.second,
            DefaultBowlerRPCProtocol.isGreaterThanUnsignedByte(data.first)
        )
    }

    @Test
    fun `test starting packet id less than zero`() {
        assertThrows<IllegalArgumentException> {
            DefaultBowlerRPCProtocol(
                server = server,
                startPacketId = -1
            )
        }
    }

    @Test
    fun `test starting packet id equal to discovery packet id`() {
        assertThrows<IllegalArgumentException> {
            DefaultBowlerRPCProtocol(
                server = server,
                startPacketId = DefaultBowlerRPCProtocol.DISCOVERY_PACKET_ID
            )
        }
    }

    @Test
    fun `test discard success before disconnect`() {
        connectProtocol()

        server.reads.addLast(
            getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_DISCARD_IN_PROGRESS)
            )
        )
        server.reads.addLast(
            getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_DISCARD_IN_PROGRESS)
            )
        )
        server.reads.addLast(
            getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_DISCARD_COMPLETE)
            )
        )

        assertTrue(protocol.disconnect().attempt().unsafeRunSync().isRight())

        assertAll(
            server.writes.map {
                {
                    val expected = getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE, byteArrayOf(4))
                    assertArrayEquals(
                        expected,
                        it.second,
                        """
                        |The sent payload:
                        |${it.second.joinToString()}
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

        server.reads.addLast(
            getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_DISCARD_IN_PROGRESS)
            )
        )
        server.reads.addLast(
            getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_DISCARD_IN_PROGRESS)
            )
        )
        server.reads.addLast(
            getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_REJECTED_GENERIC)
            )
        )

        assertTrue(protocol.disconnect().attempt().unsafeRunSync().isLeft())
    }

    @Test
    fun `test total discard failure`() {
        connectProtocol()

        server.reads.addLast(
            getPayload(
                DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                byteArrayOf(DefaultBowlerRPCProtocol.STATUS_REJECTED_GENERIC)
            )
        )

        assertTrue(protocol.disconnect().attempt().unsafeRunSync().isLeft())
    }

    @Test
    fun `allowed to disconnect with no connection`() {
        assertTrue(protocol.disconnect().attempt().unsafeRunSync().isRight())
    }

    /**
     * Connects the protocol and asserts it connected properly because no error was returned.
     */
    private fun connectProtocol() {
        assertTrue(protocol.connect().attempt().unsafeRunSync().isRight())
    }

    /**
     * Connects the protocol, runs the [operation], and asserts that:
     * 1. The operation failed because an error was returned
     * 2. No interactions happened with the device (nothing was written or read)
     *
     * @param operation The operation to perform.
     */
    private fun assertOperationFailedAndNoInteractionsWithDevice(
        operation: () -> IO<Unit>
    ) {
        connectProtocol()

        val result = operation().attempt().unsafeRunSync()

        assertAll(
            { assertTrue(result.isLeft()) },
            { assertNoInteractionsWithDevice() }
        )
    }

    /**
     * Asserts that no interactions have happened with the device (nothing written).
     */
    private fun assertNoInteractionsWithDevice() {
        server.writes.shouldBeEmpty()
    }

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
