package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned

interface AnalogIn : ProvisionedDeviceResource {

    fun read(): Double
}
