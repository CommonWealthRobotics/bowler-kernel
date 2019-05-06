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

import arrow.core.Either
import arrow.core.right
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.group.ProvisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.ProvisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.getOrFail
import org.octogonapus.ktguava.collections.toImmutableList

internal data class MockDevice(
    override val deviceId: DeviceId
) : Device {
    var connectWasCalled = false
    var disconnectWasCalled = false

    override fun connect(): Either<String, Unit> {
        connectWasCalled = true
        return Unit.right()
    }

    override fun disconnect(): Either<String, Unit> {
        disconnectWasCalled = true
        return Unit.right()
    }

    override fun isResourceInRange(resourceId: ResourceId) = true

    override fun <T : UnprovisionedDeviceResource<R>, R : ProvisionedDeviceResource> add(
        resource: T
    ) = Either.left("Not implemented")

    override fun <T : UnprovisionedDeviceResourceGroup<R>, R : ProvisionedDeviceResourceGroup> add(
        resourceGroup: T
    ) = Either.left("Not implemented")
}

internal class MockProvisionedDeviceResource(
    override val device: Device,
    override val resourceId: ResourceId
) : ProvisionedDeviceResource

internal data class MockUnprovisionedDeviceResource(
    override val device: Device,
    override val resourceId: ResourceId
) : UnprovisionedDeviceResource<MockProvisionedDeviceResource> {

    override fun provision() = MockProvisionedDeviceResource(device, resourceId)

    companion object {

        val create = { device: Device, resourceId: ResourceId ->
            MockUnprovisionedDeviceResource(device, resourceId)
        }
    }
}

internal class MockProvisionedDeviceResourceGroup(
    override val device: Device,
    override val resourceIds: ImmutableList<ResourceId>
) : ProvisionedDeviceResourceGroup

internal data class MockUnprovisionedDeviceResourceGroup(
    override val device: Device,
    override val resourceIds: ImmutableList<ResourceId>
) : UnprovisionedDeviceResourceGroup<MockProvisionedDeviceResourceGroup> {

    override fun provision() = MockProvisionedDeviceResourceGroup(device, resourceIds)

    companion object {

        val create = { device: Device, resourceIds: ImmutableList<ResourceId> ->
            MockUnprovisionedDeviceResourceGroup(device, resourceIds)
        }
    }
}

internal fun HardwareRegistry.makeDeviceOrFail(): MockDevice =
    registerDevice(
        DeviceId(
            DefaultDeviceTypes.UnknownDevice,
            DefaultConnectionMethods.RawHID(0, 0)
        )
    ) { MockDevice(it) }.getOrFail()

internal fun HardwareRegistry.makeDeviceResourceOrFail(
    device: Device,
    attachmentPoint: Byte
): MockUnprovisionedDeviceResource =
    registerDeviceResource(
        device,
        ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(attachmentPoint)),
        MockUnprovisionedDeviceResource.create
    ).getOrFail()

internal fun HardwareRegistry.makeDeviceResourceGroupOrFail(
    device: Device,
    attachmentPoints: ImmutableList<Byte>
): MockUnprovisionedDeviceResourceGroup =
    registerDeviceResourceGroup(
        device,
        attachmentPoints.map {
            ResourceId(
                DefaultResourceTypes.DigitalOut,
                DefaultAttachmentPoints.Pin(it)
            )
        }.toImmutableList(),
        MockUnprovisionedDeviceResourceGroup.create
    ).getOrFail()
