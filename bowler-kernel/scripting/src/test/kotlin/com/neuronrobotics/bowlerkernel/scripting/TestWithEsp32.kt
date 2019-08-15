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
@file:SuppressWarnings("LongMethod")

package com.neuronrobotics.bowlerkernel.scripting

import com.neuronrobotics.bowlerkernel.hardware.device.DeviceFactory
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDeviceResourceFactory
import com.neuronrobotics.bowlerkernel.hardware.protocol.SimplePacketComsProtocolFactory
import com.neuronrobotics.bowlerkernel.hardware.registry.HardwareRegistryTracker
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.octogonapus.ktguava.collections.immutableListOf
import java.net.InetAddress

internal class TestWithEsp32 {

    @Test
    @Disabled
    fun `test esp32 with script`() {
        val hardwareRegistry = HardwareRegistryTracker.createWithBaseRegistry()

        val deviceFactory = DeviceFactory(
            hardwareRegistry,
            DefaultResourceIdValidator(),
            SimplePacketComsProtocolFactory(
                DefaultResourceIdValidator()
            )
        )

        val resourceFactory = UnprovisionedDeviceResourceFactory(hardwareRegistry)

        val device = deviceFactory.makeBowlerDevice(
            DeviceId(
                DefaultDeviceTypes.Esp32wroom32,
                DefaultConnectionMethods.InternetAddress(
                    InetAddress.getByAddress(
                        listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                    )
                )
            )
        ).fold({ fail(it.toString()) }, { it })

        val unprovisionedLedGroup = resourceFactory.makeUnprovisionedDigitalOutGroup(
            device,
            immutableListOf(
                DefaultAttachmentPoints.Pin(32),
                DefaultAttachmentPoints.Pin(33)
            )
        ).fold({ fail(it.toString()) }, { it })

        device.connect().fold({ fail(it) }, { Unit })

        @Suppress("RemoveExplicitTypeArguments")
        val ledGroup = device.add(unprovisionedLedGroup).fold(
            {
                device.disconnect().fold<Nothing>({ fail(it) }, { fail("") })
            },
            { it }
        )

        repeat(100) {
            if (it % 2 == 0) {
                ledGroup.write(immutableListOf(DigitalState.LOW, DigitalState.HIGH))
            } else {
                ledGroup.write(immutableListOf(DigitalState.HIGH, DigitalState.LOW))
            }

            Thread.sleep(500)
        }

        @Suppress("RemoveExplicitTypeArguments")
        device.disconnect().fold<Nothing>({ fail(it) }, { fail("") })
    }
}
