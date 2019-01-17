/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.unprovisioned

import arrow.core.Either
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResourceFactory
import com.neuronrobotics.bowlerkernel.hardware.registry.BaseHardwareRegistry
import com.neuronrobotics.bowlerkernel.hardware.registry.RegisterError
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UnprovisionedDeviceResourceFactoryTest {

    private val registry = BaseHardwareRegistry()

    @Test
    fun `test resource id out of range`() {
        val deviceName = "A"
        val resourceId = DefaultAttachmentPoints.Pin(0)

        val device = mock<BowlerDevice> {
            on {
                isResourceInRange(
                    ResourceId(
                        DefaultResourceTypes.DigitalOut,
                        resourceId
                    )
                )
            } doReturn false
        }

        registry.registerDevice(SimpleDeviceId(deviceName)) { device }
        val error = UnprovisionedDeviceResourceFactory(registry, device)
            .makeUnprovisionedDigitalOut(resourceId)

        assertTrue(error.isLeft())
    }

    @Test
    fun `make unprovisioned led`() {
        DefaultAttachmentPoints.Pin(1).let {
            testRegistry(
                ResourceId(DefaultResourceTypes.DigitalOut, it)
            ) { makeUnprovisionedDigitalOut(it) }
        }
    }

    @Test
    fun `make unprovisioned servo`() {
        DefaultAttachmentPoints.Pin(1).let {
            testRegistry(
                ResourceId(DefaultResourceTypes.Servo, it)
            ) { makeUnprovisionedServo(it) }
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
            } doReturn SimpleDeviceId(deviceName)
        }

        registry.registerDevice(SimpleDeviceId(deviceName)) { device }

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
            } doReturn SimpleDeviceId(deviceName)
        }

        registry.registerDevice(SimpleDeviceId(deviceName)) { device }

        val resource = UnprovisionedDeviceResourceFactory(registry, device).makeResource()

        assertTrue(resource.isLeft())
        assertThat(registry.registeredDevices, hasSize(equalTo(1)))
        assertThat(registry.registeredDeviceResources.entries(), hasSize(equalTo(1)))
    }
}
