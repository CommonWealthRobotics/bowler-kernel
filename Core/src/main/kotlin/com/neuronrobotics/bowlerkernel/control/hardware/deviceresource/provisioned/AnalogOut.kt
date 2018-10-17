package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned

interface AnalogOut : ProvisionedDeviceResource {

    fun write(state: Double)
}
