package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid

data class PinNumber(
    val pinNumber: Int
) : ResourceId {
    override fun toString() = pinNumber.toString()
}
