package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource

import com.neuronrobotics.bowlerkernel.control.hardware.device.Device

interface DeviceResource {

    val device: Device

    val resourceId: ResourceId
}
