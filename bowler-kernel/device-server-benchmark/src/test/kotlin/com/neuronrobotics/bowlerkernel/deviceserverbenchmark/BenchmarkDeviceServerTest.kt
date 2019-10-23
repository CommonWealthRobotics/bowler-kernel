package com.neuronrobotics.bowlerkernel.deviceserverbenchmark

import com.neuronrobotics.bowlerkernel.deviceserver.DeviceServer
import com.neuronrobotics.bowlerkernel.deviceserver.UDPTransportLayer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.InetAddress

internal class BenchmarkDeviceServerTest {

    @Test
    @Disabled("Needs real hardware.")
    fun `test with NoopPacket on id 2`() {
        val server = DeviceServer(
            UDPTransportLayer(
                InetAddress.getByAddress(
                    listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                ),
                1866
            )
        )

        println(
            benchmarkDeviceServer(server, 2, DeviceServer::sendReceiveReliable).unsafeRunSync()
        )
    }
}
