package com.neuronrobotics.bowlerkernel.control.hardware.device

import com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId

interface Device {

    /**
     * The id of the device. This is implementation-defined for each device.
     */
    val deviceId: DeviceId

    /**
     * Open a persistent connection to the device. If the connection is already open, throw an
     * exception.
     */
    fun connect()

    /**
     * Close the persistent connection to the device. If there is no connection open, throw an
     * exception.
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
