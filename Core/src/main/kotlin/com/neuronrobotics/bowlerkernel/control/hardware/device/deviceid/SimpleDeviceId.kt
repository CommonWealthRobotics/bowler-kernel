package com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid

data class SimpleDeviceId(
    val id: String
) : DeviceId {
    override fun toString() = id
}
