/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup

import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId

data class GenericResource(
    override val device: BowlerDevice,
    override val resourceId: ResourceId
) : ProvisionedDeviceResource {

    fun read(): ByteArray = device.bowlerRPCProtocol.genericRead(resourceId).unsafeRunSync()

    fun write(payload: ByteArray): Unit =
        device.bowlerRPCProtocol.genericWrite(resourceId, payload).unsafeRunSync()

    fun writeRead(payload: ByteArray): ByteArray =
        device.bowlerRPCProtocol.genericWriteRead(resourceId, payload).unsafeRunSync()
}
