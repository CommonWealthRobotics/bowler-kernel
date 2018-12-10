/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.registry

import arrow.core.Option
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId

internal interface HardwareRegistry {

    /**
     * The currently registered devices.
     */
    val registeredDevices: ImmutableSet<DeviceId>

    /**
     * The currently registered device resources.
     */
    val registeredDeviceResources: ImmutableSetMultimap<DeviceId, ResourceId>

    /**
     * Registers a device id. Fails if the device is already registered.
     *
     * @param deviceId The device id to register.
     * @return An empty option on success, a [RegisterError] on failure.
     */
    fun registerDevice(deviceId: DeviceId): Option<RegisterError>

    /**
     * Registers a resource id attached (physically) to a device. Fails if the resource is
     * already registered or if the device is not registered.
     *
     * @param deviceId The device the resource is attached to.
     * @param resourceId The resource id to register.
     * @return An empty option on success, a [RegisterError] on failure.
     */
    fun registerDeviceResource(deviceId: DeviceId, resourceId: ResourceId): Option<RegisterError>

    /**
     * Unregisters a device id. Fails if the device is not registered.
     *
     * @param deviceId The device id to unregister.
     * @return An empty option on success, an [UnregisterError] on failure.
     */
    fun unregisterDevice(deviceId: DeviceId): Option<UnregisterError>

    /**
     * Unregisters a resource id attached (physically) to a device. Fails if the resource is
     * not registered or if the device is not registered.
     *
     * @param deviceId The device the resource is attached to.
     * @param resourceId The resource id to unregister.
     * @return An empty option on success, an [UnregisterError] on failure.
     */
    fun unregisterDeviceResource(
        deviceId: DeviceId,
        resourceId: ResourceId
    ): Option<UnregisterError>
}
