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

import com.google.common.collect.ImmutableList
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.isEmpty
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue

@DslMarker
internal annotation class ProtocolTestDsl

/**
 * A protocol test scenario. Represents a series of interactions with a device.
 *
 * @param protocol The protocol instance to test.
 * @param device The mock device to test against.
 */
@ProtocolTestDsl
internal class ProtocolScenario(
    private val protocol: SimplePacketComsProtocol,
    private val device: MockDevice
) {

    private var operation: ((SimplePacketComsProtocol) -> Unit)? = null
    private var sendPayloads: ImmutableList<ByteArray>? = null
    private var receivePayloads: ImmutableList<ByteArray>? = null

    /**
     * Configures the operation to do with the protocol, e.g. adding a resource or making an RPC
     * call.
     *
     * @param op The operation.
     * @return This scenario.
     */
    fun operation(op: (SimplePacketComsProtocol) -> Unit): ProtocolScenario {
        operation = op
        return this
    }

    /**
     * Configures what to expect the PC to send to the device.
     *
     * @param payloads The payloads the PC should send in order.
     * @return This scenario.
     */
    infix fun pcSends(payloads: () -> ImmutableList<ByteArray>): ProtocolScenario {
        sendPayloads = payloads()
        return this
    }

    /**
     * Configures what to have the device send to the PC.
     *
     * @param payloads The payloads the device should send to the PC in order.
     * @return This scenario.
     */
    infix fun deviceResponds(payloads: () -> ImmutableList<ByteArray>): ProtocolScenario {
        receivePayloads = payloads()
        return this
    }

    /**
     * Runs the tests.
     */
    fun runTest() {
        val connection = protocol.connect()
        assertTrue(connection.isEmpty())

        assertThat(
            "There should be no reads to start.",
            device.readsToSend,
            isEmpty
        )

        assertThat(
            "There should be no writes to start.",
            device.writesReceived,
            isEmpty
        )

        receivePayloads!!.forEach {
            device.readsToSend.push(it)
        }

        operation!!(protocol)

        sendPayloads!!.map {
            val write = device.writesReceived.removeFirst()
            assertArrayEquals(
                it,
                write,
                """
                |The sent payload:
                |${write.joinToString()}
                |should equal the expected payload:
                |${it.joinToString()}
                """.trimMargin()
            )
        }

        assertThat(
            "No more reads should be present.",
            device.readsToSend,
            isEmpty
        )

        assertThat(
            "No more writes should be present.",
            device.writesReceived,
            isEmpty
        )
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
    protocol: SimplePacketComsProtocol,
    device: MockDevice,
    configure: ProtocolScenario.() -> Unit
) {
    val scenario = ProtocolScenario(protocol, device)
    scenario.configure()
    scenario.runTest()
}
