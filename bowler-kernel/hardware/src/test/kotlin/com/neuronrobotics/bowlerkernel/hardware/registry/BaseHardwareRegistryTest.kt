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

package com.neuronrobotics.bowlerkernel.hardware.registry

import arrow.core.left
import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.getOrFail
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class BaseHardwareRegistryTest {

    private val registry = BaseHardwareRegistry()

    @Test
    fun `register unregistered device`() {
        registry.makeDeviceOrFail()
    }

    @Test
    fun `register device twice`() {
        registry.makeDeviceOrFail()

        val secondRegisterError = registry.registerDevice(
            DeviceId(
                DefaultDeviceTypes.UnknownDevice,
                DefaultConnectionMethods.RawHID(0, 0)
            )
        ) {
            mock<Device> {}
        }

        assertTrue(secondRegisterError.isLeft())
    }

    @Test
    fun `unregister registered device`() {
        val device = registry.makeDeviceOrFail()
        val unregisterError = registry.unregisterDevice(device)

        verify(device).disconnect()
        assertTrue(unregisterError.isEmpty())
    }

    @Test
    fun `fail to unregister registered device`() {
        val deviceId = DeviceId(
            DefaultDeviceTypes.UnknownDevice,
            DefaultConnectionMethods.RawHID(0, 0)
        )

        val device = registry.registerDevice(deviceId) {
            mock<Device> {
                on { this.deviceId } doReturn deviceId
                on { disconnect() } doReturn "Oops!".left()
            }
        }.getOrFail()

        val unregisterError = registry.unregisterDevice(device)

        assertTrue(unregisterError.nonEmpty())
    }

    @Test
    fun `unregister device twice`() {
        val device = registry.makeDeviceOrFail()

        val unregisterError = registry.unregisterDevice(device)
        val secondUnregisterError = registry.unregisterDevice(device)

        assertTrue(unregisterError.isEmpty())
        assertTrue(secondUnregisterError.nonEmpty())
    }

    @Test
    fun `unregister device with resources`() {
        val device = registry.makeDeviceOrFail()
        registry.makeDeviceResourceOrFail(device, 0)

        val unregisterError = registry.unregisterDevice(device)

        assertTrue(unregisterError.nonEmpty())
    }

    @Test
    fun `register device resource`() {
        val device = registry.makeDeviceOrFail()

        val registerError =
            registry.registerDeviceResource(
                device,
                ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(1)),
                MockUnprovisionedDeviceResource.create
            )

        assertTrue(registerError.isRight())
    }

    @Test
    fun `register device resource twice`() {
        val device = registry.makeDeviceOrFail()
        registry.makeDeviceResourceOrFail(device, 0)

        val secondRegisterError =
            registry.registerDeviceResource(
                device,
                ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(0)),
                MockUnprovisionedDeviceResource.create
            )

        assertTrue(secondRegisterError.isLeft())
    }

    @Test
    fun `register device resource without registering device first`() {
        val deviceId = DeviceId(
            DefaultDeviceTypes.UnknownDevice,
            DefaultConnectionMethods.RawHID(0, 0)
        )

        val registerError =
            registry.registerDeviceResource(
                mock<Device> {
                    on { this.deviceId } doReturn deviceId
                },
                ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(1))
            ) { _, _ ->
                mock<UnprovisionedDeviceResource<*>> {}
            }

        assertTrue(registerError.isLeft())
    }

    @Test
    fun `unregister device resource`() {
        val device = registry.makeDeviceOrFail()
        val resource = registry.makeDeviceResourceOrFail(device, 0)

        val unregisterError = registry.unregisterDeviceResource(resource)

        assertTrue(unregisterError.isEmpty())
    }

    @Test
    fun `unregister device resource twice`() {
        val device = registry.makeDeviceOrFail()
        val resource = registry.makeDeviceResourceOrFail(device, 0)

        val unregisterError = registry.unregisterDeviceResource(resource)
        val secondUnregisterError = registry.unregisterDeviceResource(resource)

        assertTrue(unregisterError.isEmpty())
        assertTrue(secondUnregisterError.nonEmpty())
    }

    @Test
    fun `unregister device resource without registering device resource first`() {
        val deviceId = DeviceId(
            DefaultDeviceTypes.UnknownDevice,
            DefaultConnectionMethods.RawHID(0, 0)
        )

        val unregisterError = registry.unregisterDeviceResource(
            MockUnprovisionedDeviceResource(
                mock<Device> {
                    on { this.deviceId } doReturn deviceId
                },
                ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(1))
            )
        )

        assertTrue(unregisterError.nonEmpty())
    }

    @Test
    fun `register two device resources on same attachment points with different types`() {
        val device = registry.makeDeviceOrFail()

        val digitalOut = registry.registerDeviceResource(
            device,
            ResourceId(
                DefaultResourceTypes.DigitalOut,
                DefaultAttachmentPoints.Pin(0)
            ),
            MockUnprovisionedDeviceResource.create
        )

        val servo = registry.registerDeviceResource(
            device,
            ResourceId(
                DefaultResourceTypes.Servo,
                DefaultAttachmentPoints.Pin(0)
            ),
            MockUnprovisionedDeviceResource.create
        )

        assertAll(
            { assertTrue(digitalOut.isRight(), "digitalOut.isRight()") },
            { assertTrue(servo.isLeft(), "servo.isLeft()") }
        )
    }
}
