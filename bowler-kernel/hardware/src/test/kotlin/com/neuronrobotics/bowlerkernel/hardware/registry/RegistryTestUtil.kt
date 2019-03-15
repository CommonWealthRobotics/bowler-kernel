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

import arrow.core.Option
import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.ProvisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResource
import org.junit.jupiter.api.Assertions.fail

internal data class MockDevice(
    override val deviceId: DeviceId
) : Device {
    var connectWasCalled = false
    var disconnectWasCalled = false

    override fun connect(): Option<String> {
        connectWasCalled = true
        return Option.empty()
    }

    override fun disconnect(): Option<String> {
        disconnectWasCalled = true
        return Option.empty()
    }

    override fun isResourceInRange(resourceId: ResourceId) = true
}

internal data class MockUnprovisionedDeviceResource(
    override val device: Device,
    override val resourceId: ResourceId
) : UnprovisionedDeviceResource {

    override fun provision() = MockProvisionedDeviceResource(device, resourceId)
}

internal class MockProvisionedDeviceResource(
    override val device: Device,
    override val resourceId: ResourceId
) : ProvisionedDeviceResource

internal fun HardwareRegistry.makeDeviceOrFail(id: String): MockDevice =
    registerDevice(SimpleDeviceId(id)) {
        MockDevice(it)
    }.fold(
        { fail<MockDevice> { it } },
        { it }
    )

internal fun HardwareRegistry.makeDeviceResourceOrFail(
    device: Device,
    attachmentPoint: Int
): MockUnprovisionedDeviceResource =
    registerDeviceResource(
        device,
        ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(attachmentPoint))
    ) { device, resource ->
        MockUnprovisionedDeviceResource(device, resource)
    }.fold(
        { fail<MockUnprovisionedDeviceResource> { it } },
        { it }
    )
