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

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResource
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableSetMultimap
import kotlin.test.assertEquals

class HardwareRegistryTrackerTest {

    private val baseRegistry = BaseHardwareRegistry()
    private val registry = HardwareRegistryTracker(baseRegistry)

    @Test
    fun `successfully register a device`() {
        registry.makeDeviceOrFail("A")

        assertAll(
            {
                assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(1)))
            },
            {
                assertEquals(
                    registry.sessionRegisteredDevices.map { it.deviceId },
                    listOf<DeviceId>(
                        SimpleDeviceId("A")
                    )
                )
            }
        )
    }

    @Test
    fun `fail to register a device`() {
        baseRegistry.makeDeviceOrFail("A")
        registry.registerDevice(
            SimpleDeviceId("A")
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
        registry.unregisterDevice(registry.makeDeviceOrFail("A"))

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
                SimpleDeviceId("A")
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
        val device = registry.makeDeviceOrFail("A")
        registry.makeDeviceResourceOrFail(device, 0)

        assertAll(
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(1)))
            },
            {
                assertEquals(
                    registry.sessionRegisteredDeviceResources.entries().map {
                        it.key.deviceId to it.value.resourceId
                    }.toMap(),
                    mapOf<DeviceId, ResourceId>(
                        SimpleDeviceId(
                            "A"
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
        registry.registerDeviceResource(
            MockDevice(SimpleDeviceId("A")),
            ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(1))
        ) { _, _ -> mock<UnprovisionedDeviceResource> {} }

        assertAll(
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0)))
            },
            {
                assertEquals(registry.sessionRegisteredDeviceResources, emptyImmutableSetMultimap())
            }
        )
    }

    @Test
    fun `successfully unregister a device resource`() {
        val device = registry.makeDeviceOrFail("A")
        registry.unregisterDeviceResource(registry.makeDeviceResourceOrFail(device, 0))

        assertAll(
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0)))
            },
            {
                assertEquals(
                    registry.sessionRegisteredDeviceResources,
                    emptyImmutableSetMultimap()
                )
            }
        )
    }

    @Test
    fun `fail to unregister a device resource`() {
        val device = registry.makeDeviceOrFail("A")
        registry.unregisterDeviceResource(
            MockUnprovisionedDeviceResource(
                device,
                ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(1))
            )
        )

        assertAll(
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0)))
            },
            {
                assertEquals(registry.sessionRegisteredDeviceResources, emptyImmutableSetMultimap())
            }
        )
    }

    @Test
    fun `unregister all devices and resources`() {
        val device = registry.makeDeviceOrFail("A")
        registry.makeDeviceResourceOrFail(device, 0)

        val unregisterErrors = registry.unregisterAllHardware()

        assertAll(
            { assertThat(unregisterErrors, hasSize(equalTo(0))) },
            { assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(0))) },
            { assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0))) }
        )
    }

    @Test
    fun `unregister all devices and resources with errors`() {
        val device = registry.makeDeviceOrFail("A")
        val resource = registry.makeDeviceResourceOrFail(device, 0)

        baseRegistry.unregisterDeviceResource(resource)
        baseRegistry.unregisterDevice(device)

        val unregisterErrors = registry.unregisterAllHardware()

        assertAll(
            { assertThat(unregisterErrors, hasSize(equalTo(2))) },
            { assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(1))) },
            { assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(1))) }
        )
    }
}
