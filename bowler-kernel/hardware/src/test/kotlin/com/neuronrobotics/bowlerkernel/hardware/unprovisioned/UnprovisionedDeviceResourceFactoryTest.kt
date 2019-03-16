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
package com.neuronrobotics.bowlerkernel.hardware.unprovisioned

import arrow.core.Either
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
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

        registry.registerDevice(
            DeviceId(
                DefaultDeviceTypes.Esp32Wroom32,
                DefaultConnectionMethods.RawHID(0, 0)
            )
        ) { device }
        val error = UnprovisionedDeviceResourceFactory(registry, device)
            .makeUnprovisionedDigitalOut(resourceId)

        assertTrue(error.isLeft())
    }

    @Test
    fun `make unprovisioned analog in`() {
        DefaultAttachmentPoints.Pin(1).let {
            testRegistry(
                ResourceId(DefaultResourceTypes.AnalogIn, it)
            ) { makeUnprovisionedAnalogIn(it) }
        }
    }

    @Test
    fun `make unprovisioned analog out`() {
        DefaultAttachmentPoints.Pin(1).let {
            testRegistry(
                ResourceId(DefaultResourceTypes.AnalogOut, it)
            ) { makeUnprovisionedAnalogOut(it) }
        }
    }

    @Test
    fun `make unprovisioned button`() {
        DefaultAttachmentPoints.Pin(1).let {
            testRegistry(
                ResourceId(DefaultResourceTypes.Button, it)
            ) { makeUnprovisionedButton(it) }
        }
    }

    @Test
    fun `make unprovisioned digital in`() {
        DefaultAttachmentPoints.Pin(1).let {
            testRegistry(
                ResourceId(DefaultResourceTypes.DigitalIn, it)
            ) { makeUnprovisionedDigitalIn(it) }
        }
    }

    @Test
    fun `make unprovisioned digital out`() {
        DefaultAttachmentPoints.Pin(1).let {
            testRegistry(
                ResourceId(DefaultResourceTypes.DigitalOut, it)
            ) { makeUnprovisionedDigitalOut(it) }
        }
    }

    @Test
    fun `make unprovisioned encoder`() {
        DefaultAttachmentPoints.PinGroup(byteArrayOf(1, 2)).let {
            testRegistry(
                ResourceId(DefaultResourceTypes.Encoder, it)
            ) { makeUnprovisionedEncoder(it) }
        }
    }

    @Test
    fun `make unprovisioned piezoelectric speaker`() {
        DefaultAttachmentPoints.Pin(1).let {
            testRegistry(
                ResourceId(DefaultResourceTypes.PiezoelectricSpeaker, it)
            ) { makeUnprovisionedPiezoelectricSpeaker(it) }
        }
    }

    @Test
    fun `make unprovisioned serial connection`() {
        DefaultAttachmentPoints.PinGroup(byteArrayOf(1, 2)).let {
            testRegistry(
                ResourceId(DefaultResourceTypes.SerialConnection, it)
            ) { makeUnprovisionedSerialConnection(it) }
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

    @Test
    fun `make unprovisioned stepper`() {
        DefaultAttachmentPoints.PinGroup(byteArrayOf(1, 2, 3, 4)).let {
            testRegistry(
                ResourceId(DefaultResourceTypes.Stepper, it)
            ) { makeUnprovisionedStepper(it) }
        }
    }

    @Test
    fun `make unprovisioned ultrasonic`() {
        DefaultAttachmentPoints.PinGroup(byteArrayOf(1, 2)).let {
            testRegistry(
                ResourceId(DefaultResourceTypes.Ultrasonic, it)
            ) { makeUnprovisionedUltrasonic(it) }
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
        val device = mock<BowlerDevice> {
            on {
                isResourceInRange(resourceId)
            } doReturn true

            on {
                deviceId
            } doReturn DeviceId(
                DefaultDeviceTypes.Esp32Wroom32,
                DefaultConnectionMethods.RawHID(0, 0)
            )
        }

        registry.registerDevice(
            DeviceId(
                DefaultDeviceTypes.Esp32Wroom32,
                DefaultConnectionMethods.RawHID(0, 0)
            )
        ) { device }

        val resource = UnprovisionedDeviceResourceFactory(registry, device).makeResource()

        assertTrue(resource.isRight())
        assertThat(registry.registeredDevices, hasSize(equalTo(1)))
        assertThat(registry.registeredDeviceResources.entries(), hasSize(equalTo(1)))
    }

    private fun testFailure(
        resourceId: ResourceId,
        makeResource: UnprovisionedDeviceResourceFactory.() -> Either<RegisterError, UnprovisionedDeviceResource>
    ) {
        val device = mock<BowlerDevice> {
            on {
                isResourceInRange(resourceId)
            } doReturn false

            on {
                deviceId
            } doReturn DeviceId(
                DefaultDeviceTypes.Esp32Wroom32,
                DefaultConnectionMethods.RawHID(0, 0)
            )
        }

        registry.registerDevice(
            DeviceId(
                DefaultDeviceTypes.Esp32Wroom32,
                DefaultConnectionMethods.RawHID(0, 0)
            )
        ) { device }

        val resource = UnprovisionedDeviceResourceFactory(registry, device).makeResource()

        assertTrue(resource.isLeft())
        assertThat(registry.registeredDevices, hasSize(equalTo(1)))
        assertThat(registry.registeredDeviceResources.entries(), hasSize(equalTo(1)))
    }
}
