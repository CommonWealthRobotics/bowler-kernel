package com.neuronrobotics.bowlerkernel.control.hardware.device

interface BowlerDeviceFactory {

    fun makeBowlerDevice(deviceId: DeviceId): BowlerDevice
}
