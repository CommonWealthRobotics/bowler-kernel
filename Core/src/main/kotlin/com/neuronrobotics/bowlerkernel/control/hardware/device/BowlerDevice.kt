package com.neuronrobotics.bowlerkernel.control.hardware.device

import com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.control.hardware.protocol.BowlerRPCProtocol

/**
 * A Bowler device is a serial device which runs the Bowler RPC protocol.
 *
 * @param deviceId The serial port the device is on.
 */
class BowlerDevice
internal constructor(
    override val deviceId: DeviceId,
    private val bowlerRPCProtocol: BowlerRPCProtocol
) : Device {

    override fun connect() {
        // TODO: Implement this properly
        bowlerRPCProtocol.write()
        bowlerRPCProtocol.read()
    }

    override fun disconnect() {
        // TODO: Implement this properly
        bowlerRPCProtocol.write()
        bowlerRPCProtocol.read()
    }

    override fun isResourceInRange(resourceId: ResourceId): Boolean {
        // TODO: Implement this properly
        bowlerRPCProtocol.write()
        bowlerRPCProtocol.read()
        return true
    }

    override fun toString() = deviceId.toString()
}
