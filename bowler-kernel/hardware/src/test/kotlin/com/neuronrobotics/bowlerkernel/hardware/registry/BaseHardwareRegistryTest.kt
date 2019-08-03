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

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.group.ProvisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.ProvisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.getOrFail
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class BaseHardwareRegistryTest {

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

            override fun connect() = Unit.right()

            override fun disconnect() = "Oops!".left()

            override fun isResourceInRange(resourceId: ResourceId) = true

            override fun <T : UnprovisionedDeviceResource<R>, R : ProvisionedDeviceResource> add(
                resource: T
            ) = Either.left("Not implemented")

            override fun <T : UnprovisionedDeviceResourceGroup<R>, R : ProvisionedDeviceResourceGroup> add(
                resourceGroup: T
            ) = Either.left("Not implemented")
        }

        val device = registry.registerDevice(
            DeviceId(
                DefaultDeviceTypes.UnknownDevice,
                DefaultConnectionMethods.RawHID(0, 0)
            )
        ) { ThrowingMockDevice(it) }.getOrFail()

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
        val registerError =
            registry.registerDeviceResource(
                MockDevice(
                    DeviceId(
                        DefaultDeviceTypes.UnknownDevice,
                        DefaultConnectionMethods.RawHID(0, 0)
                    )
                ),
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
        val unregisterError = registry.unregisterDeviceResource(
            MockUnprovisionedDeviceResource(
                MockDevice(
                    DeviceId(
                        DefaultDeviceTypes.UnknownDevice,
                        DefaultConnectionMethods.RawHID(0, 0)
                    )
                ),
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
