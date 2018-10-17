package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned

import com.neuronrobotics.bowlerkernel.control.hardware.device.Device
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.ResourceId

class LED(
        override val device: Device,
        override val resourceId: ResourceId
) : DigitalOut {

    override fun write(state: DigitalState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
