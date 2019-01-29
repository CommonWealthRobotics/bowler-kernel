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

import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId

interface Device {

    /**
     * The id of the device. This is implementation-defined for each device.
     */
    val deviceId: DeviceId

    /**
     * Open a persistent connection to the device. If the connection is already open, throw an
     * exception. If the connection could not be opened, throw an exception.
     */
    fun connect()

    /**
     * Close the persistent connection to the device. If there is no connection open, throw an
     * exception. If the connection could not be closed, throw an exception.
     */
    fun disconnect()

    /**
     * Returns whether the [resourceId] is in the valid range of resources for this device.
     *
     * @param resourceId The id of a resource on this device.
     * @return Whether the resource id is in the valid range of resources for this device.
     */
    fun isResourceInRange(resourceId: ResourceId): Boolean
}
