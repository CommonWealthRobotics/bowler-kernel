package com.neuronrobotics.bowlerkernel.control.hardware.device

import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.ResourceId

interface Device {

    val deviceId: DeviceId

    fun connect()

    fun disconnect()

    fun isResourceInRange(resourceId: ResourceId): Boolean
}
