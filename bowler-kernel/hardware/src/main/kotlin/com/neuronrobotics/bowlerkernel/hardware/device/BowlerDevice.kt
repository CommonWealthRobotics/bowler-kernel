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
import arrow.core.extensions.either.monad.flatMap
import arrow.core.flatMap
import arrow.effects.IO
import arrow.effects.extensions.io.semigroup.maybeCombine
import arrow.effects.liftIO
import arrow.typeclasses.Semigroup
import com.google.common.base.Throwables
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.group.ProvisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.ProvisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
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
        if (resourceId.resourceType is DefaultResourceTypes) {
            // Only check with the device if its a known resource type because other resource types
            // can be weird. Just let the RPC validate those.
            IO.just(deviceId.deviceType.isResourceInRange(resourceId)).maybeCombine(
                object : Semigroup<Boolean> {
                    override fun Boolean.combine(b: Boolean) = this && b
                },
                bowlerRPCProtocol.isResourceInRange(resourceId)
            )
        } else {
            bowlerRPCProtocol.isResourceInRange(resourceId)
        }

    override fun <T : UnprovisionedDeviceResource<R>, R : ProvisionedDeviceResource> add(
        resource: T
    ): IO<R> {
        val id = resource.resourceId

        return resourceIdValidator.validateIsReadType(id.resourceType).liftIO().flatMap {
            it.fold(
                {
                    // Not a read
                    resourceIdValidator.validateIsWriteType(id.resourceType).liftIO().flatMap {
                        it.fold(
                            {
                                // Not a read, not a write
                                IO.raiseError<R>(
                                    UnsupportedOperationException(
                                        """
                                        |Could not add resource because it neither a read type nor a write type:
                                        |$resource
                                        """.trimMargin()
                                    )
                                )
                            },
                            {
                                // Not a read, is a write
                                bowlerRPCProtocol.addWrite(id).attempt().errorOnLeft()
                                    .map { resource.provision() }
                            }
                        )
                    }
                },
                {
                    // Is a read
                    resourceIdValidator.validateIsWriteType(id.resourceType).liftIO().flatMap {
                        it.fold(
                            {
                                // Is a read, not a write
                                bowlerRPCProtocol.addRead(id).attempt().errorOnLeft()
                                    .map { resource.provision() }
                            },
                            {
                                // Is a read, is a write
                                bowlerRPCProtocol.addWriteRead(id).attempt().errorOnLeft()
                                    .map { resource.provision() }
                            }
                        )
                    }
                }
            )
        }
    }

    @SuppressWarnings("ReturnCount")
    override fun <T : UnprovisionedDeviceResourceGroup<R>, R : ProvisionedDeviceResourceGroup> add(
        resourceGroup: T
    ): IO<R> {
        if (resourceGroup.resourceIds.distinct() != resourceGroup.resourceIds) {
            return IO.raiseError(
                UnsupportedOperationException(
                    """
                    |The provided resource ids must be unique:
                    |${resourceGroup.resourceIds}
                    """.trimMargin()
                )
            )
        }

        val resourceIds = resourceGroup.resourceIds.toImmutableSet()

        val allReadResources = resourceIds.map {
            resourceIdValidator.validateIsReadType(it.resourceType)
        }.fold(true) { acc, elem ->
            acc && elem.isRight()
        }

        val allWriteResources = resourceIds.map {
            resourceIdValidator.validateIsWriteType(it.resourceType)
        }.fold(true) { acc, elem ->
            acc && elem.isRight()
        }

        return IO.defer {
            if (allReadResources && allWriteResources) {
                bowlerRPCProtocol.addWriteReadGroup(resourceIds).attempt().errorOnLeft()
                    .map { resourceGroup.provision() }
            } else if (allReadResources) {
                bowlerRPCProtocol.addReadGroup(resourceIds).attempt().errorOnLeft()
                    .map { resourceGroup.provision() }
            } else if (allWriteResources) {
                bowlerRPCProtocol.addWriteGroup(resourceIds).attempt().errorOnLeft()
                    .map { resourceGroup.provision() }
            } else {
                IO.raiseError(
                    UnsupportedOperationException(
                        """
                        |Could not add resources because they are neither all read types nor all write types:
                        |${resourceIds.joinToString(separator = "\n")}
                        """.trimMargin()
                    )
                )
            }
        }
    }

    override fun toString() = """BowlerDevice(deviceId=$deviceId)"""

    private fun <A, B> IO<Either<A, B>>.errorOnLeft(): IO<B> = flatMap {
        when (it) {
            is Either.Left -> IO.raiseError(
                UnsupportedOperationException(
                    when (it.a) {
                        is Throwable -> Throwables.getStackTraceAsString(it.a as Throwable)
                        else -> it.a.toString()
                    }
                )
            )

            is Either.Right -> IO.just(it.b)
        }
    }
}
