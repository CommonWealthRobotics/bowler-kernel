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
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.group.ProvisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.ProvisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.protocol.BowlerRPCProtocol
import org.octogonapus.ktguava.collections.toImmutableSet

/**
 * A Bowler device is a serial device which runs the Bowler RPC protocol.
 *
 * @param deviceId The serial port the device is on.
 */
class BowlerDevice
internal constructor(
    override val deviceId: DeviceId,
    val bowlerRPCProtocol: BowlerRPCProtocol,
    private val resourceIdValidator: ResourceIdValidator
) : Device {

    override fun connect() = bowlerRPCProtocol.connect()

    override fun disconnect() = bowlerRPCProtocol.disconnect()

    override fun isResourceInRange(resourceId: ResourceId) =
        deviceId.deviceType.isResourceInRange(resourceId) &&
            bowlerRPCProtocol.isResourceInRange(resourceId)

    override fun <T : UnprovisionedDeviceResource<R>, R : ProvisionedDeviceResource> add(
        resource: T
    ): Either<String, R> {
        val id = resource.resourceId

        val readError = resourceIdValidator.validateIsReadType(id.resourceType).flatMap {
            bowlerRPCProtocol.addRead(id).also {
                when (it) {
                    is Either.Left -> return it
                }
            }
        }

        val writeError = resourceIdValidator.validateIsWriteType(id.resourceType).flatMap {
            bowlerRPCProtocol.addWrite(id).also {
                when (it) {
                    is Either.Left -> return it
                }
            }
        }

        return when {
            readError is Either.Left && writeError is Either.Left ->
                """
                |Could not add resource because it neither a read type nor a write type:
                |$resource
                """.trimMargin().left()

            else -> resource.provision().right()
        }
    }

    @SuppressWarnings("ReturnCount")
    override fun <T : UnprovisionedDeviceResourceGroup<R>, R : ProvisionedDeviceResourceGroup> add(
        resourceGroup: T
    ): Either<String, R> {
        if (resourceGroup.resourceIds.distinct() != resourceGroup.resourceIds) {
            return """
                |The provided resource ids must be unique:
                |${resourceGroup.resourceIds}
            """.trimMargin().left()
        }

        val resourceIds = resourceGroup.resourceIds.toImmutableSet()

        val allReadResources = resourceIds.map {
            resourceIdValidator.validateIsReadType(it.resourceType)
        }.fold(true) { acc, elem ->
            acc && elem.isRight()
        }

        if (allReadResources) {
            bowlerRPCProtocol.addReadGroup(resourceIds).also {
                when (it) {
                    is Either.Left -> return it
                }
            }
        }

        val allWriteResources = resourceIds.map {
            resourceIdValidator.validateIsWriteType(it.resourceType)
        }.fold(true) { acc, elem ->
            acc && elem.isRight()
        }

        if (allWriteResources) {
            bowlerRPCProtocol.addWriteGroup(resourceIds).also {
                when (it) {
                    is Either.Left -> return it
                }
            }
        }

        return if (!allReadResources && !allWriteResources) {
            """
            |Could not add resources because they are neither all read types nor all write
            |types:
            |${resourceIds.joinToString(separator = "\n")}
            """.trimMargin().left()
        } else {
            resourceGroup.provision().right()
        }
    }

    override fun toString() = """BowlerDevice(deviceId=$deviceId)"""
}
