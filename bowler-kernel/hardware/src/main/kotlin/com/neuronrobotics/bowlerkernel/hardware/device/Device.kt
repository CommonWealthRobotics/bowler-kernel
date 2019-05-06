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
package com.neuronrobotics.bowlerkernel.hardware.device

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.group.ProvisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.ProvisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDeviceResource

interface Device {

    /**
     * The id of the device. This is implementation-defined for each device.
     */
    val deviceId: DeviceId

    /**
     * Opens a persistent connection to the device. An error is returned if the connection is
     * already open or could not be opened.
     *
     * @return An error if there is a connection problem.
     */
    fun connect(): Either<String, Unit>

    /**
     * Closes the persistent connection to the device. Does nothing if there is no connection open.
     *
     * @return An error if there is a disconnect problem.
     */
    fun disconnect(): Either<String, Unit>

    /**
     * Returns whether the [resourceId] is in the valid range of resources for this device.
     *
     * @param resourceId The id of a resource on this device.
     * @return Whether the resource id is in the valid range of resources for this device.
     */
    fun isResourceInRange(resourceId: ResourceId): Boolean

    /**
     * Adds the [resource] to the device.
     *
     * @return The provisioned resource, or an error.
     */
    fun <T : UnprovisionedDeviceResource<R>, R : ProvisionedDeviceResource> add(
        resource: T
    ): Either<String, R>

    /**
     * Adds the [resourceGroup] to the device.
     *
     * @return The provisioned resource group, or an error.
     */
    fun <T : UnprovisionedDeviceResourceGroup<R>, R : ProvisionedDeviceResourceGroup> add(
        resourceGroup: T
    ): Either<String, R>
}
