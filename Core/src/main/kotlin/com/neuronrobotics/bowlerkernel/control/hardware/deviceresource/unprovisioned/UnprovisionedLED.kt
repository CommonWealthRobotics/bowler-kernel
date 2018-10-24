package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.control.hardware.device.Device
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.ProvisionError
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned.LED

class UnprovisionedLED(
    override val device: Device,
    override val resourceId: ResourceId
) : UnprovisionedDeviceResource {

    override fun provision(): Either<ProvisionError, LED> {
        return Either.right(LED(device, resourceId))
    }
}
