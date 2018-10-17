package com.neuronrobotics.bowlerkernel.control.hardware.registry

import arrow.core.Option
import com.neuronrobotics.bowlerkernel.control.hardware.device.DeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.ResourceId

internal interface HardwareRegistry {

    fun registerDevice(deviceId: DeviceId): Option<RegisterError>

    fun registerDeviceResource(deviceId: DeviceId, resourceId: ResourceId): Option<RegisterError>

    fun unregisterDevice(deviceId: DeviceId): Option<UnregisterError>

    fun unregisterDeviceResource(
        deviceId: DeviceId,
        resourceId: ResourceId
    ): Option<UnregisterError>
}
