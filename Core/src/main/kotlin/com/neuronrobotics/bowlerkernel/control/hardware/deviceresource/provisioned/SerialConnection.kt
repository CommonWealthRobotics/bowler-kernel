package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned

interface SerialConnection : ProvisionedDeviceResource {

    fun write(data: String)

    fun read(): String
}
