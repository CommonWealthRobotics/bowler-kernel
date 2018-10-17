package com.neuronrobotics.bowlerkernel.control.hardware.registry

import arrow.core.Option
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.natpryce.hamkrest.isEmpty
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.spy
import org.junit.jupiter.api.Test

class HardwareRegistryTrackerTest {

    private val underlyingRegistry = StaticHardwareRegistry()
    private val registry = HardwareRegistryTracker(underlyingRegistry)

    @Test
    fun `unregister all devices and resources`() {
        registry.registerDevice("A")
        registry.registerDeviceResource("A", "C")
        registry.registerDeviceResource("A", "D")

        registry.registerDevice("B")
        registry.registerDeviceResource("B", "C")
        registry.registerDeviceResource("B", "D")

        val unregisterErrors = registry.unregisterAllHardware()
        assertThat(unregisterErrors, isEmpty)
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
}
