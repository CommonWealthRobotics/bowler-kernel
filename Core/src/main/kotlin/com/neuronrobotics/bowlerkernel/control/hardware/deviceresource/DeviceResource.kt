package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource

import com.neuronrobotics.bowlerkernel.control.hardware.device.Device
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId

/**
 * A resource attached (physically) to a [Device].
 */
interface DeviceResource {

    /**
     * The [Device] this resource is attached to.
     */
    val device: Device

    /**
     * The id of this resource. Typically a pin number.
     */
    val resourceId: ResourceId
}
