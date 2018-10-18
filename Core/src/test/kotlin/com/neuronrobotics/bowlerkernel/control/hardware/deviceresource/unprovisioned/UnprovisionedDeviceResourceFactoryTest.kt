package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.neuronrobotics.bowlerkernel.control.hardware.device.Device
import com.neuronrobotics.bowlerkernel.control.hardware.registry.RegisterError
import com.neuronrobotics.bowlerkernel.control.hardware.registry.StaticHardwareRegistry
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UnprovisionedDeviceResourceFactoryTest {

    private val registry = StaticHardwareRegistry()

    @Test
    fun `all interfaces get registered`() {
        testRegistry("1") { makeUnprovisionedLED(1) }
    }

    private fun testRegistry(
        resourceId: String,
        makeResource: UnprovisionedDeviceResourceFactory.() -> Either<RegisterError, UnprovisionedDeviceResource>
    ) {
        val deviceName = "A"

        val device = mock<Device> {
            on {
                isResourceInRange(resourceId)
            } doReturn true

            on {
                deviceId
            } doReturn deviceName
        }

        registry.registerDevice(deviceName)
        val resource = UnprovisionedDeviceResourceFactory(registry, device).makeResource()

        assertTrue(resource.isRight())
        assertThat(registry.registeredDevices, hasSize(equalTo(1)))
        assertThat(registry.registeredDeviceResources.entries(), hasSize(equalTo(1)))
    }
}
