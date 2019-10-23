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
package com.neuronrobotics.bowlerkernel.deviceserverbenchmark

import com.neuronrobotics.bowlerkernel.deviceserver.DefaultDeviceServer
import com.neuronrobotics.bowlerkernel.deviceserver.UDPTransportLayer
import java.net.InetAddress
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class BenchmarkDeviceServerTest {

    @Test
    @Disabled("Needs real hardware.")
    fun `test with reliable NoopPacket on id 2`() {
        val server = DefaultDeviceServer(
            UDPTransportLayer(
                InetAddress.getByAddress(
                    listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                ),
                1866
            )
        )

        server.addReliable(2)

        println("Test with reliable transport:")
        println(benchmarkDeviceServer(server, 61, 2).unsafeRunSync())
    }

    @Test
    @Disabled("Needs real hardware.")
    fun `test with unreliable NoopPacket on id 2`() {
        val server = DefaultDeviceServer(
            UDPTransportLayer(
                InetAddress.getByAddress(
                    listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                ),
                1866
            )
        )

        server.addUnreliable(2, 10)

        println("Test with unreliable transport:")
        println(benchmarkDeviceServer(server, 61, 2).unsafeRunSync())
    }
}
