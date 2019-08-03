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
import arrow.core.Option
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.DeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.DeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.registry.error.RegisterDeviceError
import com.neuronrobotics.bowlerkernel.hardware.registry.error.RegisterDeviceResourceError
import com.neuronrobotics.bowlerkernel.hardware.registry.error.RegisterDeviceResourceGroupError
import com.neuronrobotics.bowlerkernel.hardware.registry.error.UnregisterDeviceError
import com.neuronrobotics.bowlerkernel.hardware.registry.error.UnregisterDeviceResourceError
import com.neuronrobotics.bowlerkernel.hardware.registry.error.UnregisterDeviceResourceGroupError

/**
 * A utility to keep track of what hardware is in use.
 */
interface HardwareRegistry {

    /**
     * The currently registered devices.
     */
    val registeredDevices: ImmutableSet<Device>

    /**
     * The currently registered device resources.
     */
    val registeredDeviceResources: ImmutableSetMultimap<Device,
        Either<DeviceResource, DeviceResourceGroup>>

    /**
     * Registers a device id. Fails if the device is already registered.
     *
     * @param deviceId The device id to register.
     * @param makeDevice A lambda to construct the device.
     * @return An empty option on success, a [RegisterError] on failure.
     */
    fun <T : Device> registerDevice(
        deviceId: DeviceId,
        makeDevice: (DeviceId) -> T
    ): Either<RegisterDeviceError, T>

    /**
     * Registers a resource id attached (physically) to a device. Fails if the resource is
     * already registered or if the device is not registered.
     *
     * @param device The device the resource is attached to.
     * @param resourceId The resource id to register.
     * @param makeResource A lambda to construct the unprovisioned resource.
     * @return An empty option on success, a [RegisterError] on failure.
     */
    fun <D : Device, T : UnprovisionedDeviceResource<*>> registerDeviceResource(
        device: D,
        resourceId: ResourceId,
        makeResource: (D, ResourceId) -> T
    ): Either<RegisterDeviceResourceError, T>

    /**
     * Registers a resource group attached (physically) to a device. Fails if the resource group is
     * already registered or if the device is not registered.
     *
     * @param device The device the resource is attached to.
     * @param resourceIds The resource ids in the group to register.
     * @param makeResourceGroup A lambda to construct the unprovisioned resource group.
     * @return An empty option on success, a [RegisterError] on failure.
     */
    fun <D : Device, T : UnprovisionedDeviceResourceGroup<*>> registerDeviceResourceGroup(
        device: D,
        resourceIds: ImmutableList<ResourceId>,
        makeResourceGroup: (D, ImmutableList<ResourceId>) -> T
    ): Either<RegisterDeviceResourceGroupError, T>

    /**
     * Unregisters and disconnects a device. Fails if the device is not registered or if the
     * device still has registered resources.
     *
     * @param device The device to unregister.
     * @return An empty option on success, an [UnregisterError] on failure.
     */
    fun unregisterDevice(device: Device): Option<UnregisterDeviceError>

    /**
     * Unregisters a resource attached (physically) to a device. Fails if the resource is
     * not registered or if the device is not registered.
     *
     * @param resource The resource to unregister.
     * @return An empty option on success, an [UnregisterError] on failure.
     */
    fun unregisterDeviceResource(
        resource: DeviceResource
    ): Option<UnregisterDeviceResourceError>

    /**
     * Unregisters a resource group attached (physically) to a device. Fails if the resource
     * group is not registered or if the device is not registered.
     *
     * @param resourceGroup The resource group to unregister.
     * @return An empty option on success, an [UnregisterError] on failure.
     */
    fun unregisterDeviceResourceGroup(
        resourceGroup: DeviceResourceGroup
    ): Option<UnregisterDeviceResourceGroupError>
}
