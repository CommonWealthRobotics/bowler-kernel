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

import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.fail

@DslMarker
internal annotation class ProtocolTestDsl

/**
 * A protocol test scenario. Represents a series of interactions with a device.
 *
 * @param protocol The protocol instance to test.
 * @param server The mock device server to test against.
 */
@ProtocolTestDsl
internal class ProtocolScenario(
    private val protocol: DefaultBowlerRPCProtocol,
    private val server: MockDeviceServer
) {

    private var operation: ((DefaultBowlerRPCProtocol) -> Unit)? = null
    private var sendPayloads: List<ByteArray>? = null
    private var receivePayloads: List<ByteArray>? = null

    /**
     * Configures the operation to do with the protocol, e.g. adding a resource or making an RPC
     * call.
     *
     * @param op The operation.
     * @return This scenario.
     */
    fun operation(op: (DefaultBowlerRPCProtocol) -> Unit): ProtocolScenario {
        operation = op
        return this
    }

    /**
     * Configures what to expect the PC to send to the device.
     *
     * @param payloads The payloads the PC should send in order.
     * @return This scenario.
     */
    infix fun pcSends(payloads: () -> List<ByteArray>): ProtocolScenario {
        sendPayloads = payloads()
        return this
    }

    /**
     * Configures what to have the device send to the PC.
     *
     * @param payloads The payloads the device should send to the PC in order.
     * @return This scenario.
     */
    infix fun deviceResponds(payloads: () -> List<ByteArray>): ProtocolScenario {
        receivePayloads = payloads()
        return this
    }

    /**
     * Runs the tests.
     */
    @SuppressWarnings("LongMethod")
    fun runTest() {
        val connection = protocol.connect().attempt().unsafeRunSync()
        assertTrue(connection.isRight())

        // There should be no reads or writes to start
        server.reads.shouldBeEmpty()
        server.writes.shouldBeEmpty()

        receivePayloads!!.forEach {
            server.reads.addLast(it)
        }

        operation!!(protocol)

        sendPayloads!!.map {
            if (server.writes.isEmpty()) {
                fail {
                    """
                    |Expected a sent payload:
                    |${it.joinToString()}
                    |but got nothing.
                    """.trimMargin()
                }
            } else {
                val write = server.writes.pop()
                assertArrayEquals(
                    it,
                    write.second,
                    """
                    |The sent payload:
                    |${write.second.joinToString()}
                    |should equal the expected payload:
                    |${it.joinToString()}
                    """.trimMargin()
                )
            }
        }

        // No more reads or writes should be present
        server.reads.shouldBeEmpty()
        server.writes.shouldBeEmpty()
    }
}

/**
 * Configures and runs a protocol test.
 *
 * @param protocol The protocol instance to test.
 * @param device The mock device to test against.
 * @param configure The test configuration.
 */
@ProtocolTestDsl
internal fun protocolTest(
    protocol: DefaultBowlerRPCProtocol,
    server: MockDeviceServer,
    configure: ProtocolScenario.() -> Unit
) {
    val scenario = ProtocolScenario(protocol, server)
    scenario.configure()
    scenario.runTest()
}
