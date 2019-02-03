package com.neuronrobotics.bowlerkernel.hardware

import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.protocol.SimplePacketComsProtocol
import edu.wpi.SimplePacketComs.device.UdpDevice
import org.junit.jupiter.api.Test
import org.octogonapus.guavautil.collections.emptyImmutableList
import org.octogonapus.guavautil.collections.immutableListOf
import java.net.InetAddress

internal class Test {

    @Test
    fun `test esp32`() {
        val testPin = ResourceId(
            DefaultResourceTypes.DigitalIn,
            DefaultAttachmentPoints.Pin(35)
        )

        val rpc = SimplePacketComsProtocol(
            object : UdpDevice(
                InetAddress.getByAddress(
                    listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                )
            ) {
            },
            1,
            immutableListOf(testPin),
            emptyImmutableList(),
            emptyImmutableList()
        )

        rpc.connect()
        for (i in 0 until 5) {
            println(rpc.analogRead(testPin))
            Thread.sleep(10)
        }
    }
}
