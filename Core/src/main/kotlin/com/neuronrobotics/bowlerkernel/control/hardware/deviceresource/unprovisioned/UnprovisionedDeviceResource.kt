package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.DeviceResource
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.ProvisionError
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned.ProvisionedDeviceResource

interface UnprovisionedDeviceResource : DeviceResource {

    fun provision(): Either<ProvisionError, ProvisionedDeviceResource>
}
