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
package com.neuronrobotics.bowlerkernel.hardware.device

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.ProvisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.protocol.BowlerRPCProtocol

/**
 * A Bowler device is a serial device which runs the Bowler RPC protocol.
 *
 * @param deviceId The serial port the device is on.
 */
class BowlerDevice
internal constructor(
    override val deviceId: DeviceId,
    val bowlerRPCProtocol: BowlerRPCProtocol,
    val resourceIdValidator: ResourceIdValidator
) : Device {

    override fun connect() = bowlerRPCProtocol.connect()

    override fun disconnect() = bowlerRPCProtocol.disconnect()

    override fun isResourceInRange(resourceId: ResourceId) =
        bowlerRPCProtocol.isResourceInRange(resourceId)

    fun add(resource: UnprovisionedDeviceResource): Either<String, ProvisionedDeviceResource> {
        val id = resource.resourceId

        val readError = resourceIdValidator.validateIsReadType(id).toSwappedEither().flatMap {
            bowlerRPCProtocol.addRead(id).toSwappedEither()
        }

        val writeError = resourceIdValidator.validateIsWriteType(id).toSwappedEither().flatMap {
            bowlerRPCProtocol.addWrite(id).toSwappedEither()
        }

        return if (readError.isLeft() && writeError.isLeft()) {
            """
            |Could not add resource because it neither a read type nor a write type:
            |$resource
            """.trimMargin().left()
        } else {
            resource.provision().right()
        }
    }

    fun addGroup(
        resources: ImmutableSet<UnprovisionedDeviceResource>
    ): Either<String, ImmutableSet<ProvisionedDeviceResource>> {
        TODO("not implemented")
    }

    private fun <T> Option<T>.toSwappedEither(): Either<T, Unit> = toEither { Unit }.swap()

    override fun toString() = """`$deviceId`"""
}
