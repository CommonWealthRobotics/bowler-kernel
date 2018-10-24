package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid

/**
 * The id of a resource attached (physically) to a device.
 */
interface ResourceId {

    /**
     * The value of this device id as a string.
     */
    override fun toString(): String
}
