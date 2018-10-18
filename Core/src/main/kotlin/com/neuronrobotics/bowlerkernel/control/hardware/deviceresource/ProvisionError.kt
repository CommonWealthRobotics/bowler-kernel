package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource

/**
 * An error encountered while provisioning a [DeviceResource].
 */
data class ProvisionError(
    val errorString: String
)
