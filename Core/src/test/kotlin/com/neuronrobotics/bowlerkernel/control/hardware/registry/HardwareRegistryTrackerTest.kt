/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.registry

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.neuronrobotics.bowlerkernel.control.hardware.device.Device
import com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.SimpleResourceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResource
import com.neuronrobotics.bowlerkernel.util.emptyImmutableSetMultimap
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
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
        registry.unregisterDevice(MockDevice(SimpleDeviceId("A")))

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
        registry.makeDeviceResourceOrFail(device, "B")

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
                        ) to SimpleResourceId(
                            "B"
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
            SimpleResourceId("B")
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
        registry.unregisterDeviceResource(registry.makeDeviceResourceOrFail(device, "B"))

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
            MockUnprovisionedDeviceResource(device, SimpleResourceId("B"))
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
        registry.makeDeviceResourceOrFail(device, "B")

        val unregisterErrors = registry.unregisterAllHardware()

        assertAll(
            { assertThat(unregisterErrors, hasSize(equalTo(0))) },
            { assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(0))) },
            { assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0))) }
        )
    }
}
