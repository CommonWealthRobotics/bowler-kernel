package com.neuronrobotics.bowlerkernel.control.hardware.registry

import arrow.core.Option
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.natpryce.hamkrest.isEmpty
import com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.SimpleResourceId
import com.neuronrobotics.bowlerkernel.util.emptyImmutableSetMultimap
import com.neuronrobotics.bowlerkernel.util.immutableSetMultimapOf
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
        val mockRegistry = mock<BaseHardwareRegistry> {
            on {
                registerDevice(
                    SimpleDeviceId(
                        "A"
                    )
                )
            } doReturn Option.empty()
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.registerDevice(
            SimpleDeviceId(
                "A"
            )
        )
        assertAll(
            {
                assertThat(registry.sessionRegisteredDevices, hasSize(equalTo(1)))
            },
            {
                assertEquals(
                    registry.sessionRegisteredDevices,
                    setOf<DeviceId>(
                        SimpleDeviceId(
                            "A"
                        )
                    )
                )
            }
        )
    }

    @Test
    fun `fail to register a device`() {
        val mockRegistry = mock<BaseHardwareRegistry> {
            on {
                registerDevice(
                    SimpleDeviceId(
                        "A"
                    )
                )
            } doReturn Option.just(RegisterError(""))
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.registerDevice(
            SimpleDeviceId(
                "A"
            )
        )
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
        val mockRegistry = mock<BaseHardwareRegistry> {
            on {
                unregisterDevice(
                    SimpleDeviceId(
                        "A"
                    )
                )
            } doReturn Option.empty()
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.unregisterDevice(
            SimpleDeviceId(
                "A"
            )
        )
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
        val mockRegistry = mock<BaseHardwareRegistry> {
            on {
                unregisterDevice(
                    SimpleDeviceId(
                        "A"
                    )
                )
            } doReturn Option.just(UnregisterError(""))
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.unregisterDevice(
            SimpleDeviceId(
                "A"
            )
        )
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
        val mockRegistry = mock<BaseHardwareRegistry> {
            on {
                registerDeviceResource(
                    SimpleDeviceId(
                        "A"
                    ),
                    SimpleResourceId(
                        "B"
                    )
                )
            } doReturn Option.empty()
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.registerDeviceResource(
            SimpleDeviceId("A"),
            SimpleResourceId(
                "B"
            )
        )
        assertAll(
            {
                assertThat(registry.sessionRegisteredDeviceResources.entries(), hasSize(equalTo(1)))
            },
            {
                assertEquals(
                    registry.sessionRegisteredDeviceResources,
                    immutableSetMultimapOf<DeviceId, ResourceId>(
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
        val mockRegistry = mock<BaseHardwareRegistry> {
            on {
                registerDeviceResource(
                    SimpleDeviceId(
                        "A"
                    ),
                    SimpleResourceId(
                        "B"
                    )
                )
            } doReturn Option.just(RegisterError(""))
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.registerDeviceResource(
            SimpleDeviceId("A"),
            SimpleResourceId(
                "B"
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
    fun `successfully unregister a device resource`() {
        val mockRegistry = mock<BaseHardwareRegistry> {
            on {
                unregisterDeviceResource(
                    SimpleDeviceId(
                        "A"
                    ),
                    SimpleResourceId(
                        "B"
                    )
                )
            } doReturn Option.empty()
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.unregisterDeviceResource(
            SimpleDeviceId("A"),
            SimpleResourceId(
                "B"
            )
        )
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
        val mockRegistry = mock<BaseHardwareRegistry> {
            on {
                unregisterDeviceResource(
                    SimpleDeviceId(
                        "A"
                    ),
                    SimpleResourceId(
                        "B"
                    )
                )
            } doReturn Option.just(UnregisterError(""))
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.unregisterDeviceResource(
            SimpleDeviceId("A"),
            SimpleResourceId(
                "B"
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
        val registry = HardwareRegistryTracker(BaseHardwareRegistry())

        registry.registerDevice(
            SimpleDeviceId(
                "A"
            )
        )
        registry.registerDeviceResource(
            SimpleDeviceId("A"),
            SimpleResourceId(
                "B"
            )
        )

        val unregisterErrors = registry.unregisterAllHardware()
        assertThat(unregisterErrors, hasSize(equalTo(0)))
    }

    @Test
    fun `unregister devices and resources never registered`() {
        val mockRegistry = spy<BaseHardwareRegistry> {
            on {
                unregisterDevice(
                    SimpleDeviceId(
                        "A"
                    )
                )
            } doReturn Option.just(UnregisterError(""))

            on {
                unregisterDeviceResource(
                    SimpleDeviceId(
                        "A"
                    ),
                    SimpleResourceId(
                        "B"
                    )
                )
            } doReturn Option.just(UnregisterError(""))
        }

        val registry = HardwareRegistryTracker(mockRegistry)
        registry.registerDevice(
            SimpleDeviceId(
                "A"
            )
        )
        registry.registerDeviceResource(
            SimpleDeviceId("A"),
            SimpleResourceId(
                "B"
            )
        )

        val unregisterErrors = registry.unregisterAllHardware()
        assertThat(unregisterErrors, hasSize(equalTo(2)))
    }

    @Nested
    inner class IntegrationTests {

        private val staticRegistry = BaseHardwareRegistry()
        private val registry = HardwareRegistryTracker(staticRegistry)

        @Test
        fun `device integration test`() {
            registry.registerDevice(
                SimpleDeviceId(
                    "A"
                )
            )
            registry.registerDeviceResource(
                SimpleDeviceId("A"),
                SimpleResourceId(
                    "B"
                )
            )
            registry.registerDevice(
                SimpleDeviceId(
                    "C"
                )
            )
            registry.registerDeviceResource(
                SimpleDeviceId("C"),
                SimpleResourceId(
                    "D"
                )
            )
            registry.registerDeviceResource(
                SimpleDeviceId("C"),
                SimpleResourceId(
                    "E"
                )
            )

            assertAll(
                {
                    assertEquals(
                        setOf<DeviceId>(
                            SimpleDeviceId(
                                "A"
                            ),
                            SimpleDeviceId(
                                "C"
                            )
                        ),
                        registry.registeredDevices
                    )
                },
                { assertEquals(registry.registeredDevices, registry.sessionRegisteredDevices) },
                {
                    assertEquals(
                        immutableSetMultimapOf<DeviceId, ResourceId>(
                            SimpleDeviceId(
                                "A"
                            ) to SimpleResourceId(
                                "B"
                            ),
                            SimpleDeviceId(
                                "C"
                            ) to SimpleResourceId(
                                "D"
                            ),
                            SimpleDeviceId(
                                "C"
                            ) to SimpleResourceId(
                                "E"
                            )
                        ),
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
