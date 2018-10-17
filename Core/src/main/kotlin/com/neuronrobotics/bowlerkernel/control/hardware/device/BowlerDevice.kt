package com.neuronrobotics.bowlerkernel.control.hardware.device

import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.ResourceId

class BowlerDevice
internal constructor(
    override val deviceId: DeviceId
) : Device {

    override fun connect() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun disconnect() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun isResourceInRange(resourceId: ResourceId): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
