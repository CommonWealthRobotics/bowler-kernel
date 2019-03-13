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
package com.neuronrobotics.bowlerkernel.hardware.registry

import arrow.core.Option
import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResource
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class BaseHardwareRegistryTest {

    private val registry = BaseHardwareRegistry()

    @Test
    fun `register unregistered device`() {
        registry.makeDeviceOrFail("A")
    }

    @Test
    fun `register device twice`() {
        registry.makeDeviceOrFail("A")

        val secondRegisterError = registry.registerDevice(
            SimpleDeviceId(
                "A"
            )
        ) {
            mock<Device> {}
        }

        assertTrue(secondRegisterError.isLeft())
    }

    @Test
    fun `unregister registered device`() {
        val device = registry.makeDeviceOrFail("A")
        val unregisterError = registry.unregisterDevice(device)

        assertAll(
            { assertTrue(unregisterError.isEmpty()) },
            { assertTrue(device.disconnectWasCalled) }
        )
    }

    @Test
    fun `fail to unregister registered device`() {
        class ThrowingMockDevice(
            override val deviceId: DeviceId
        ) : Device {

            override fun connect(): Option<String> = Option.empty()

            override fun disconnect() {
                throw IllegalStateException("Oops!")
            }

            override fun isResourceInRange(resourceId: ResourceId) = true
        }

        val device = registry.registerDevice(SimpleDeviceId("A")) {
            ThrowingMockDevice(it)
        }.fold(
            { fail<ThrowingMockDevice> { it } },
            { it }
        )

        val unregisterError = registry.unregisterDevice(device)

        assertTrue(unregisterError.nonEmpty())
    }

    @Test
    fun `unregister device twice`() {
        val device = registry.makeDeviceOrFail("A")

        val unregisterError = registry.unregisterDevice(device)
        val secondUnregisterError = registry.unregisterDevice(device)

        assertTrue(unregisterError.isEmpty())
        assertTrue(secondUnregisterError.nonEmpty())
    }

    @Test
    fun `unregister device with resources`() {
        val device = registry.makeDeviceOrFail("A")
        registry.makeDeviceResourceOrFail(device, 0)

        val unregisterError = registry.unregisterDevice(device)

        assertTrue(unregisterError.nonEmpty())
    }

    @Test
    fun `register device resource`() {
        val device = registry.makeDeviceOrFail("A")

        val registerError =
            registry.registerDeviceResource(
                device,
                ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(1))
            ) { device, resource ->
                MockUnprovisionedDeviceResource(
                    device,
                    resource
                )
            }

        assertTrue(registerError.isRight())
    }

    @Test
    fun `register device resource twice`() {
        val device = registry.makeDeviceOrFail("A")
        registry.makeDeviceResourceOrFail(device, 0)

        val secondRegisterError =
            registry.registerDeviceResource(
                device,
                ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(0))
            ) { device, resource ->
                MockUnprovisionedDeviceResource(
                    device,
                    resource
                )
            }

        assertTrue(secondRegisterError.isLeft())
    }

    @Test
    fun `register device resource without registering device first`() {
        val registerError =
            registry.registerDeviceResource(
                MockDevice(SimpleDeviceId("A")),
                ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(1))
            ) { _, _ ->
                mock<UnprovisionedDeviceResource> {}
            }

        assertTrue(registerError.isLeft())
    }

    @Test
    fun `unregister device resource`() {
        val device = registry.makeDeviceOrFail("A")
        val resource = registry.makeDeviceResourceOrFail(device, 0)

        val unregisterError = registry.unregisterDeviceResource(resource)

        assertTrue(unregisterError.isEmpty())
    }

    @Test
    fun `unregister device resource twice`() {
        val device = registry.makeDeviceOrFail("A")
        val resource = registry.makeDeviceResourceOrFail(device, 0)

        val unregisterError = registry.unregisterDeviceResource(resource)
        val secondUnregisterError = registry.unregisterDeviceResource(resource)

        assertTrue(unregisterError.isEmpty())
        assertTrue(secondUnregisterError.nonEmpty())
    }

    @Test
    fun `unregister device resource without registering device resource first`() {
        val unregisterError = registry.unregisterDeviceResource(
            MockUnprovisionedDeviceResource(
                MockDevice(SimpleDeviceId("A")),
                ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(1))
            )
        )

        assertTrue(unregisterError.nonEmpty())
    }

    @Test
    fun `register two device resources on same attachment points with different types`() {
        val device = registry.makeDeviceOrFail("A")

        val digitalOut = registry.registerDeviceResource(
            device,
            ResourceId(
                DefaultResourceTypes.DigitalOut,
                DefaultAttachmentPoints.Pin(0)
            )
        ) { device, resource ->
            MockUnprovisionedDeviceResource(device, resource)
        }

        val servo = registry.registerDeviceResource(
            device,
            ResourceId(
                DefaultResourceTypes.Servo,
                DefaultAttachmentPoints.Pin(0)
            )
        ) { device, resource ->
            MockUnprovisionedDeviceResource(device, resource)
        }

        assertAll(
            { assertTrue(digitalOut.isRight(), "digitalOut.isRight()") },
            { assertTrue(servo.isLeft(), "servo.isLeft()") }
        )
    }
}
