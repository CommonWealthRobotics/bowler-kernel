package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid

data class SimpleResourceId(
    val id: String
) : ResourceId {
    override fun toString() = id
}
