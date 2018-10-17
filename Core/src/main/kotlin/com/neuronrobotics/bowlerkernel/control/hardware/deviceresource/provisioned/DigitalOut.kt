package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned

interface DigitalOut : ProvisionedDeviceResource {

    fun write(state: DigitalState)
}
