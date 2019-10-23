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
import com.neuronrobotics.bowlerkernel.deviceserver.DefaultDeviceServer
import com.neuronrobotics.bowlerkernel.deviceserver.getPayload
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

internal class UdpDeviceServerIntegTest {

    private val payloadSize = 61

    @Test
    fun `test unreliable transport seq 0 ack 0`() {
        val transportLayer = MockTransportLayer()
        val server = DefaultDeviceServer(transportLayer)
        server.addUnreliable(2, 10)

        transportLayer.reads.addLast(byteArrayOf(2, 0, 0, *getPayload(payloadSize)))
        server.sendReceiveUnreliable(2, getPayload(payloadSize), 10)
        assertArrayEquals(
            byteArrayOf(2, 0, 0, *getPayload(payloadSize)),
            transportLayer.writes.pop()
        )
    }

    @Test
    fun `test unreliable transport seq 0 ack 1`() {
        val transportLayer = MockTransportLayer()
        val server = DefaultDeviceServer(transportLayer)
        server.addUnreliable(2, 10)

        // Unreliable transport will just accept the first read (not looking at ACK)
        transportLayer.reads.addLast(byteArrayOf(2, 0, 1, *getPayload(payloadSize)))
        server.sendReceiveUnreliable(2, getPayload(payloadSize), 10)
        assertArrayEquals(
            byteArrayOf(2, 0, 0, *getPayload(payloadSize)),
            transportLayer.writes.pop()
        )
    }

    @Test
    fun `test reliable transport seq 0 ack 0`() {
        val transportLayer = MockTransportLayer()
        val server = DefaultDeviceServer(transportLayer)
        server.addReliable(2)

        transportLayer.reads.addLast(byteArrayOf(2, 0, 0, *getPayload(payloadSize)))
        server.sendReceiveReliable(2, getPayload(payloadSize))
        assertArrayEquals(
            byteArrayOf(2, 0, 1, *getPayload(payloadSize)),
            transportLayer.writes.pop()
        )
    }

    @Test
    fun `test reliable transport seq 0 ack 1 seq 0 ack 0`() {
        val transportLayer = MockTransportLayer()
        val server = DefaultDeviceServer(transportLayer)
        server.addReliable(2)

        transportLayer.reads.addLast(byteArrayOf(2, 0, 1, *getPayload(payloadSize)))
        transportLayer.reads.addLast(byteArrayOf(2, 0, 0, *getPayload(payloadSize)))
        server.sendReceiveReliable(2, getPayload(payloadSize))
        assertArrayEquals(
            byteArrayOf(2, 0, 1, *getPayload(payloadSize)),
            transportLayer.writes.pop()
        )
    }

    @Test
    fun `test reliable transport seq 0 ack 0 seq 1 ack 1`() {
        val transportLayer = MockTransportLayer()
        val server = DefaultDeviceServer(transportLayer)
        server.addReliable(2)

        transportLayer.reads.addLast(byteArrayOf(2, 0, 0, *getPayload(payloadSize)))
        server.sendReceiveReliable(2, getPayload(payloadSize))
        assertArrayEquals(
            byteArrayOf(2, 0, 1, *getPayload(payloadSize)),
            transportLayer.writes.pop()
        )

        transportLayer.reads.addLast(byteArrayOf(2, 1, 1, *getPayload(payloadSize)))
        server.sendReceiveReliable(2, getPayload(payloadSize))
        assertArrayEquals(
            byteArrayOf(2, 1, 0, *getPayload(payloadSize)),
            transportLayer.writes.pop()
        )
    }
}
