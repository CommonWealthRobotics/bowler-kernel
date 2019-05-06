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

import arrow.core.None
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.DeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDeviceResource
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableSetMultimap
import org.octogonapus.ktguava.collections.immutableListOf

class HardwareRegistryTrackerTest {

    private val baseRegistry = BaseHardwareRegistry()
    private val registry = HardwareRegistryTracker(baseRegistry)

    @Test
    fun `successfully register a device`() {
        registry.makeDeviceOrFail()

        assertAll(
            {
                assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(1)))
            },
            {
                assertEquals(
                    registry.sessionRegisteredDevices.map { it.deviceId },
                    listOf(
                        DeviceId(
                            DefaultDeviceTypes.UnknownDevice,
                            DefaultConnectionMethods.RawHID(0, 0)
                        )
                    )
                )
            }
        )
    }

    @Test
    fun `fail to register a device`() {
        baseRegistry.makeDeviceOrFail()
        registry.registerDevice(
            DeviceId(
                DefaultDeviceTypes.UnknownDevice,
                DefaultConnectionMethods.RawHID(0, 0)
            )
        ) { mock<MockDevice> {} }

        assertAll(
            {
                assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(0)))
            },
            {
                assertEquals(registry.sessionRegisteredDevices, emptySet<Device>())
            }
        )
    }

    @Test
    fun `successfully unregister a device`() {
        registry.unregisterDevice(registry.makeDeviceOrFail())

        assertAll(
            {
                assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(0)))
            },
            {
                assertEquals(registry.sessionRegisteredDevices, emptySet<Device>())
            }
        )
    }

    @Test
    fun `fail to unregister a device`() {
        registry.unregisterDevice(
            MockDevice(
                DeviceId(
                    DefaultDeviceTypes.UnknownDevice,
                    DefaultConnectionMethods.RawHID(0, 0)
                )
            )
        )

        assertAll(
            {
                assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(0)))
            },
            {
                assertEquals(registry.sessionRegisteredDevices, emptySet<Device>())
            }
        )
    }

    @Test
    fun `successfully register a device resource`() {
        val device = registry.makeDeviceOrFail()
        registry.makeDeviceResourceOrFail(device, 0)

        assertAll(
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(1)))
            },
            {
                assertEquals(
                    registry.sessionRegisteredDeviceResources.entries().map {
                        it.key.deviceId to it.value.fold({ it.resourceId }, { fail() })
                    }.toMap(),
                    mapOf(
                        DeviceId(
                            DefaultDeviceTypes.UnknownDevice,
                            DefaultConnectionMethods.RawHID(0, 0)
                        ) to ResourceId(
                            DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(0)
                        )
                    )
                )
            }
        )
    }

    @Test
    fun `fail to register a device resource`() {
        val device = MockDevice(
            DeviceId(
                DefaultDeviceTypes.UnknownDevice,
                DefaultConnectionMethods.RawHID(0, 0)
            )
        )

        val resourceId = ResourceId(
            DefaultResourceTypes.DigitalOut,
            DefaultAttachmentPoints.Pin(1)
        )

        registry.registerDeviceResource(device, resourceId) { _, _ ->
            mock<UnprovisionedDeviceResource<*>> {}
        }

        assertAll(
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0)))
            },
            {
                assertEquals(
                    registry.sessionRegisteredDeviceResources,
                    emptyImmutableSetMultimap<Device, DeviceResource>()
                )
            }
        )
    }

    @Test
    fun `successfully unregister a device resource`() {
        val device = registry.makeDeviceOrFail()
        val resource = registry.makeDeviceResourceOrFail(device, 0)
        val result = registry.unregisterDeviceResource(resource)

        assertAll(
            {
                assertTrue(result is None)
            },
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0)))
            },
            {
                assertEquals(
                    registry.sessionRegisteredDeviceResources,
                    emptyImmutableSetMultimap<Device, DeviceResource>()
                )
            }
        )
    }

    @Test
    fun `successfully unregister a device resource group`() {
        val device = registry.makeDeviceOrFail()
        val resourceGroup = registry.makeDeviceResourceGroupOrFail(device, immutableListOf(0, 1))
        val result = registry.unregisterDeviceResourceGroup(resourceGroup)

        assertAll(
            {
                assertTrue(result is None)
            },
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0)))
            },
            {
                assertEquals(
                    registry.sessionRegisteredDeviceResources,
                    emptyImmutableSetMultimap<Device, DeviceResource>()
                )
            }
        )
    }

    @Test
    fun `fail to unregister a device resource`() {
        val device = registry.makeDeviceOrFail()
        val resource = MockUnprovisionedDeviceResource(
            device,
            ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(1))
        )

        registry.unregisterDeviceResource(resource)

        assertAll(
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0)))
            },
            {
                assertEquals(
                    registry.sessionRegisteredDeviceResources,
                    emptyImmutableSetMultimap<Device, DeviceResource>()
                )
            }
        )
    }

    @Test
    fun `fail to unregister a device resource group`() {
        val device = registry.makeDeviceOrFail()
        val resourceGroup = MockUnprovisionedDeviceResourceGroup(
            device,
            immutableListOf(
                ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(1)),
                ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(2))
            )
        )

        registry.unregisterDeviceResourceGroup(resourceGroup)

        assertAll(
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0)))
            },
            {
                assertEquals(
                    registry.sessionRegisteredDeviceResources,
                    emptyImmutableSetMultimap<Device, DeviceResource>()
                )
            }
        )
    }

    @Test
    fun `unregister all devices and resources`() {
        val device = registry.makeDeviceOrFail()
        registry.makeDeviceResourceOrFail(device, 0)
        registry.makeDeviceResourceGroupOrFail(device, immutableListOf(1, 2))

        val unregisterErrors = registry.unregisterAllHardware()

        assertAll(
            { assertThat(unregisterErrors, hasSize(equalTo(0))) },
            { assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(0))) },
            { assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0))) }
        )
    }

    @Test
    fun `unregister all devices and resources with errors`() {
        val device = registry.makeDeviceOrFail()
        val resource = registry.makeDeviceResourceOrFail(device, 0)
        val resourceGroup = registry.makeDeviceResourceGroupOrFail(device, immutableListOf(1, 2))

        baseRegistry.unregisterDeviceResource(resource)
        baseRegistry.unregisterDeviceResourceGroup(resourceGroup)
        baseRegistry.unregisterDevice(device)

        val unregisterErrors = registry.unregisterAllHardware()

        assertAll(
            { assertThat(unregisterErrors, hasSize(equalTo(3))) },
            { assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(1))) },
            { assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(2))) }
        )
    }

    @Test
    fun `unregister all devices and resources with some errors`() {
        val device = registry.makeDeviceOrFail()
        val resource = registry.makeDeviceResourceOrFail(device, 0)
        registry.makeDeviceResourceGroupOrFail(device, immutableListOf(1, 2))

        baseRegistry.unregisterDeviceResource(resource)

        val unregisterErrors = registry.unregisterAllHardware()

        assertAll(
            { assertThat(unregisterErrors, hasSize(equalTo(1))) },
            { assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(0))) },
            { assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0))) }
        )
    }
}
