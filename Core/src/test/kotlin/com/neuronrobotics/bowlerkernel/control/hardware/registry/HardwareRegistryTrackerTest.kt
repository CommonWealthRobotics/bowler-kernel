package com.neuronrobotics.bowlerkernel.control.hardware.registry

import arrow.core.Option
import com.google.common.collect.ImmutableSetMultimap
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.natpryce.hamkrest.isEmpty
import com.neuronrobotics.bowlerkernel.control.hardware.device.DeviceId
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals

class HardwareRegistryTrackerTest {

    @Test
    fun `successfully register a device`() {
        val mockRegistry = mock<StaticHardwareRegistry> {
            on {
                registerDevice("A")
            } doReturn Option.empty()
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.registerDevice("A")
        assertAll(
            {
                assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(1)))
            },
            {
                assertEquals(registry.sessionRegisteredDevices, setOf("A"))
            }
        )
    }

    @Test
    fun `fail to register a device`() {
        val mockRegistry = mock<StaticHardwareRegistry> {
            on {
                registerDevice("A")
            } doReturn Option.just(RegisterError(""))
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.registerDevice("A")
        assertAll(
            {
                assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(0)))
            },
            {
                assertEquals(registry.sessionRegisteredDevices, emptySet<DeviceId>())
            }
        )
    }

    @Test
    fun `successfully unregister a device`() {
        val mockRegistry = mock<StaticHardwareRegistry> {
            on {
                unregisterDevice("A")
            } doReturn Option.empty()
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.unregisterDevice("A")
        assertAll(
            {
                assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(0)))
            },
            {
                assertEquals(registry.sessionRegisteredDevices, emptySet<DeviceId>())
            }
        )
    }

    @Test
    fun `fail to unregister a device`() {
        val mockRegistry = mock<StaticHardwareRegistry> {
            on {
                unregisterDevice("A")
            } doReturn Option.just(UnregisterError(""))
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.unregisterDevice("A")
        assertAll(
            {
                assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(0)))
            },
            {
                assertEquals(registry.sessionRegisteredDevices, emptySet<DeviceId>())
            }
        )
    }

    @Test
    fun `successfully register a device resource`() {
        val mockRegistry = mock<StaticHardwareRegistry> {
            on {
                registerDeviceResource("A", "B")
            } doReturn Option.empty()
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.registerDeviceResource("A", "B")
        assertAll(
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(1)))
            },
            {
                assertEquals(
                    registry.sessionRegisteredDeviceResources,
                    ImmutableSetMultimap.of("A", "B")
                )
            }
        )
    }

    @Test
    fun `fail to register a device resource`() {
        val mockRegistry = mock<StaticHardwareRegistry> {
            on {
                registerDeviceResource("A", "B")
            } doReturn Option.just(RegisterError(""))
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.registerDeviceResource("A", "B")
        assertAll(
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0)))
            },
            {
                assertEquals(registry.sessionRegisteredDeviceResources, ImmutableSetMultimap.of())
            }
        )
    }

    @Test
    fun `successfully unregister a device resource`() {
        val mockRegistry = mock<StaticHardwareRegistry> {
            on {
                unregisterDeviceResource("A", "B")
            } doReturn Option.empty()
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.unregisterDeviceResource("A", "B")
        assertAll(
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0)))
            },
            {
                assertEquals(
                    registry.sessionRegisteredDeviceResources,
                    ImmutableSetMultimap.of()
                )
            }
        )
    }

    @Test
    fun `fail to unregister a device resource`() {
        val mockRegistry = mock<StaticHardwareRegistry> {
            on {
                unregisterDeviceResource("A", "B")
            } doReturn Option.just(UnregisterError(""))
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.unregisterDeviceResource("A", "B")
        assertAll(
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(0)))
            },
            {
                assertEquals(registry.sessionRegisteredDeviceResources, ImmutableSetMultimap.of())
            }
        )
    }

    @Test
    fun `unregister all devices and resources`() {
        val registry = HardwareRegistryTracker(StaticHardwareRegistry())

        registry.registerDevice("A")
        registry.registerDeviceResource("A", "B")

        val unregisterErrors = registry.unregisterAllHardware()
        assertThat(unregisterErrors, hasSize(equalTo(0)))
    }

    @Test
    fun `unregister devices and resources never registered`() {
        val mockRegistry = spy<StaticHardwareRegistry> {
            on {
                unregisterDevice("A")
            } doReturn Option.just(UnregisterError(""))

            on {
                unregisterDeviceResource("A", "B")
            } doReturn Option.just(UnregisterError(""))
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.registerDevice("A")
        registry.registerDeviceResource("A", "B")

        val unregisterErrors = registry.unregisterAllHardware()
        assertThat(unregisterErrors, hasSize(equalTo(2)))
    }

    @Nested
    inner class IntegrationTests {

        private val staticRegistry = StaticHardwareRegistry()
        private val registry = HardwareRegistryTracker(staticRegistry)

        @Test
        fun `device integration test`() {
            registry.registerDevice("A")
            registry.registerDeviceResource("A", "B")
            registry.registerDevice("C")
            registry.registerDeviceResource("C", "D")
            registry.registerDeviceResource("C", "E")

            assertAll(
                { assertEquals(setOf("A", "C"), registry.registeredDevices) },
                { assertEquals(registry.registeredDevices, registry.sessionRegisteredDevices) },
                {
                    assertEquals(
                        ImmutableSetMultimap.of("A", "B", "C", "D", "C", "E"),
                        registry.registeredDeviceResources
                    )
                },
                {
                    assertEquals(
                        registry.registeredDeviceResources,
                        registry.sessionRegisteredDeviceResources
                    )
                }
            )

            registry.unregisterAllHardware()

            assertAll(
                { assertThat(registry.registeredDevices, isEmpty) },
                { assertThat(registry.sessionRegisteredDevices, isEmpty) },
                { assertThat(registry.registeredDeviceResources.entries(), isEmpty) },
                { assertThat(registry.sessionRegisteredDeviceResources.entries(), isEmpty) }
            )
        }
    }
}
