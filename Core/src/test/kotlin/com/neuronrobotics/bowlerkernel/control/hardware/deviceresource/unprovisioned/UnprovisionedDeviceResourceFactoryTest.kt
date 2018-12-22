/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.neuronrobotics.bowlerkernel.control.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.PinNumber
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.control.hardware.registry.BaseHardwareRegistry
import com.neuronrobotics.bowlerkernel.control.hardware.registry.RegisterError
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UnprovisionedDeviceResourceFactoryTest {

    private val registry = BaseHardwareRegistry()

    @Test
    fun `make unprovisioned led`() {
        PinNumber(1).let {
            testRegistry(it) { makeUnprovisionedLED(it) }
        }
    }

    @Test
    fun `make unprovisioned servo`() {
        PinNumber(1).let {
            testRegistry(it) { makeUnprovisionedServo(it) }
        }
    }

    private fun testRegistry(
        resourceId: ResourceId,
        makeResource: UnprovisionedDeviceResourceFactory.() -> Either<RegisterError, UnprovisionedDeviceResource>
    ) {
        testSuccess(resourceId, makeResource)
        testFailure(resourceId, makeResource)
    }

    private fun testSuccess(
        resourceId: ResourceId,
        makeResource: UnprovisionedDeviceResourceFactory.() -> Either<RegisterError, UnprovisionedDeviceResource>
    ) {
        val deviceName = "A"

        val device = mock<BowlerDevice> {
            on {
                isResourceInRange(resourceId)
            } doReturn true

            on {
                deviceId
            } doReturn SimpleDeviceId(
                deviceName
            )
        }

        registry.registerDevice(
            SimpleDeviceId(
                deviceName
            )
        )
        val resource = UnprovisionedDeviceResourceFactory(registry, device).makeResource()

        assertTrue(resource.isRight())
        assertThat(registry.registeredDevices, hasSize(equalTo(1)))
        assertThat(registry.registeredDeviceResources.entries(), hasSize(equalTo(1)))
    }

    private fun testFailure(
        resourceId: ResourceId,
        makeResource: UnprovisionedDeviceResourceFactory.() -> Either<RegisterError, UnprovisionedDeviceResource>
    ) {
        val deviceName = "A"

        val device = mock<BowlerDevice> {
            on {
                isResourceInRange(resourceId)
            } doReturn false

            on {
                deviceId
            } doReturn SimpleDeviceId(
                deviceName
            )
        }

        registry.registerDevice(
            SimpleDeviceId(
                deviceName
            )
        )
        val resource = UnprovisionedDeviceResourceFactory(registry, device).makeResource()

        assertTrue(resource.isLeft())
        assertThat(registry.registeredDevices, hasSize(equalTo(1)))
        assertThat(registry.registeredDeviceResources.entries(), hasSize(equalTo(1)))
    }
}
