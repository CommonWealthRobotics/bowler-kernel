package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned

interface DigitalIn : ProvisionedDeviceResource {

    fun read(): DigitalState
}
