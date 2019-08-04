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
@file:SuppressWarnings("LargeClass", "TooManyFunctions", "LongMethod")

package com.neuronrobotics.bowlerkernel.hardware.registry

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableSetMultimap
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.toImmutableList

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

        val result = registry.registerDeviceResource(device, resourceId) { _, _ ->
            mock<UnprovisionedDeviceResource<*>> {}
        }

        assertAll(
            {
                assertTrue(result is Either.Left)
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

        assertTrue(registry.unregisterDeviceResource(resource) is Some)

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

        assertTrue(registry.unregisterDeviceResourceGroup(resourceGroup) is Some)

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

        assertTrue(baseRegistry.unregisterDeviceResource(resource) is None)
        assertTrue(baseRegistry.unregisterDeviceResourceGroup(resourceGroup) is None)
        assertTrue(baseRegistry.unregisterDevice(device) is None)

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

        assertTrue(baseRegistry.unregisterDeviceResource(resource) is None)

        val unregisterErrors = registry.unregisterAllHardware()

        assertAll(
            { assertThat(unregisterErrors, hasSize(equalTo(1))) },
            { assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(0))) },
            { assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0))) }
        )
    }

    @Nested
    inner class TestWithNestedTrackers {

        private val secondRegistry = HardwareRegistryTracker(registry)

        @Test
        fun `successfully registry a device`() {
            secondRegistry.makeDeviceOrFail()

            assertAll(
                {
                    assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(1)))
                },
                {
                    assertThat(secondRegistry.sessionRegisteredDevices, hasSize(equalTo(1)))
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
                },
                {
                    assertEquals(
                        secondRegistry.sessionRegisteredDevices.map { it.deviceId },
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
        fun `fail to register in the second layer`() {
            val device = registry.makeDeviceOrFail()
            registry.makeDeviceResourceOrFail(device, 0)
            registry.makeDeviceResourceGroupOrFail(device, immutableListOf(1, 2))

            val actual = secondRegistry.registerDeviceResourceGroup(
                device,
                immutableListOf(1, 2).map {
                    ResourceId(
                        DefaultResourceTypes.DigitalOut,
                        DefaultAttachmentPoints.Pin(it.toByte())
                    )
                }.toImmutableList(),
                MockUnprovisionedDeviceResourceGroup.create
            )

            assertTrue(actual is Either.Left)
        }

        @Test
        fun `unregister all from the second layer`() {
            val device = secondRegistry.makeDeviceOrFail()
            secondRegistry.makeDeviceResourceOrFail(device, 0)
            secondRegistry.makeDeviceResourceGroupOrFail(device, immutableListOf(1, 2))

            assertAll(
                { assertThat(baseRegistry.registeredDevices, hasSize(equalTo(1))) },
                {
                    assertThat(
                        baseRegistry.registeredDeviceResources.entries(),
                        hasSize(equalTo(2))
                    )
                },
                { assertThat(secondRegistry.sessionRegisteredDevices, hasSize(equalTo(1))) },
                {
                    assertThat(
                        secondRegistry.sessionRegisteredDeviceResources.entries(),
                        hasSize(equalTo(2))
                    )
                },
                { assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(1))) },
                {
                    assertThat(
                        registry.sessionRegisteredDeviceResources.entries(),
                        hasSize(equalTo(2))
                    )
                }
            )

            val unregisterErrors = secondRegistry.unregisterAllHardware()

            assertAll(
                { assertThat(unregisterErrors, hasSize(equalTo(0))) },
                { assertThat(secondRegistry.sessionRegisteredDevices, hasSize(equalTo(0))) },
                {
                    assertThat(
                        secondRegistry.sessionRegisteredDeviceResources.entries(),
                        hasSize(equalTo(0))
                    )
                },
                { assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(0))) },
                {
                    assertThat(
                        registry.sessionRegisteredDeviceResources.entries(),
                        hasSize(equalTo(0))
                    )
                }
            )
        }

        @Test
        fun `unregister all from the second layer with some errors`() {
            val device = secondRegistry.makeDeviceOrFail()
            val resource = secondRegistry.makeDeviceResourceOrFail(device, 0)
            secondRegistry.makeDeviceResourceGroupOrFail(device, immutableListOf(1, 2))

            assertAll(
                { assertThat(baseRegistry.registeredDevices, hasSize(equalTo(1))) },
                {
                    assertThat(
                        baseRegistry.registeredDeviceResources.entries(),
                        hasSize(equalTo(2))
                    )
                },
                { assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(1))) },
                {
                    assertThat(
                        registry.sessionRegisteredDeviceResources.entries(),
                        hasSize(equalTo(2))
                    )
                },
                { assertThat(secondRegistry.sessionRegisteredDevices, hasSize(equalTo(1))) },
                {
                    assertThat(
                        secondRegistry.sessionRegisteredDeviceResources.entries(),
                        hasSize(equalTo(2))
                    )
                }
            )

            val unregisterResourceError = baseRegistry.unregisterDeviceResource(resource)
            assertTrue(unregisterResourceError is None)

            val unregisterErrors = secondRegistry.unregisterAllHardware()

            assertAll(
                { assertThat(unregisterErrors, hasSize(equalTo(1))) },
                { assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(0))) },
                {
                    val actual = registry.sessionRegisteredDeviceResources.entries()
                    assertThat(
                        """
                        Expected an empty list but got:
                        ${actual.joinToString("\n")}
                        """.trimIndent(),
                        actual,
                        hasSize(equalTo(0))
                    )
                },
                { assertThat(secondRegistry.sessionRegisteredDevices, hasSize(equalTo(0))) },
                {
                    assertThat(
                        secondRegistry.sessionRegisteredDeviceResources.entries(),
                        hasSize(equalTo(0))
                    )
                }
            )
        }
    }
}
