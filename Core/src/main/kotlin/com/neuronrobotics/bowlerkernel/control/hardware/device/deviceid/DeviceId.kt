package com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid

/**
 * The id of a physical device (i.e. a microcontroller).
 */
interface DeviceId {

    /**
     * The value of this device id as a string.
     */
    override fun toString(): String
}
