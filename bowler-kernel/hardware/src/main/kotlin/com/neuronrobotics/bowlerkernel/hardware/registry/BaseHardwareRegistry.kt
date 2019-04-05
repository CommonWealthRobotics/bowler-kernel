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
package com.neuronrobotics.bowlerkernel.hardware.registry

import arrow.core.Either
import arrow.core.Option
import arrow.core.Try
import arrow.core.left
import arrow.core.right
import com.google.common.collect.MultimapBuilder
import com.google.common.collect.SetMultimap
import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.DeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResource
import org.octogonapus.ktguava.collections.toImmutableSet
import org.octogonapus.ktguava.collections.toImmutableSetMultimap

/**
 * The base implementation of [HardwareRegistry].
 */
internal class BaseHardwareRegistry
internal constructor() : HardwareRegistry {

    private val internalRegisteredDeviceIds: MutableSet<DeviceId> = mutableSetOf()
    private val internalRegisteredDevices: MutableSet<Device> = mutableSetOf()

    @Suppress("UnstableApiUsage")
    private val internalRegisteredDeviceResourceIds: SetMultimap<DeviceId, ResourceId> =
        MultimapBuilder.hashKeys().hashSetValues().build()
    @Suppress("UnstableApiUsage")
    private val internalRegisteredDeviceResources: SetMultimap<Device, DeviceResource> =
        MultimapBuilder.hashKeys().hashSetValues().build()

    override val registeredDevices
        get() = internalRegisteredDevices.toImmutableSet()

    override val registeredDeviceResources
        get() = internalRegisteredDeviceResources.toImmutableSetMultimap()

    override fun <T : Device> registerDevice(
        deviceId: DeviceId,
        makeDevice: (DeviceId) -> T
    ): Either<RegisterError, T> {
        return when {
            internalRegisteredDeviceIds.contains(deviceId) ->
                """
                |Cannot register device $deviceId because the device is already registered.
                """.trimMargin().left()

            else -> {
                internalRegisteredDeviceIds.add(deviceId)

                makeDevice(deviceId).also {
                    internalRegisteredDevices.add(it)
                }.right()
            }
        }
    }

    override fun <D : Device, T : UnprovisionedDeviceResource> registerDeviceResource(
        device: D,
        resourceId: ResourceId,
        makeResource: (D, ResourceId) -> T
    ): Either<RegisterError, T> {
        return when {
            !internalRegisteredDeviceIds.contains(device.deviceId) ->
                """
                |Cannot register resource $resourceId on device ${device.deviceId} because device
                |${device.deviceId} is not registered.
                """.trimMargin().left()

            internalRegisteredDeviceResourceIds.containsEntry(
                device.deviceId,
                resourceId
            ) ->
                """
                |Cannot register resource $resourceId on device ${device.deviceId} because the
                |resource is already registered.
                """.trimMargin().left()

            internalRegisteredDeviceResourceIds.values().any {
                it.attachmentPoint == resourceId.attachmentPoint
            } ->
                """
                |Cannot register resource $resourceId on device ${device.deviceId} because there
                |is already a resource on the same attachment point.
                """.trimMargin().left()

            else -> {
                internalRegisteredDeviceResourceIds.put(device.deviceId, resourceId)

                makeResource(device, resourceId).also {
                    internalRegisteredDeviceResources.put(device, it)
                }.right()
            }
        }
    }

    override fun unregisterDevice(device: Device): Option<UnregisterError> {
        return when {
            !internalRegisteredDeviceIds.contains(device.deviceId) -> Option.just(
                """
                |Cannot unregister device ${device.deviceId} because the device is not registered.
                """.trimMargin()
            )

            internalRegisteredDeviceResourceIds[device.deviceId].isNotEmpty() -> Option.just(
                """
                |Cannot unregister device ${device.deviceId} because there are registered
                |resources attached to it.
                """.trimMargin()
            )

            else -> Try {
                device.disconnect()
            }.toEither().fold(
                {
                    Option.just(it.message ?: "")
                },
                {
                    internalRegisteredDeviceIds.remove(device.deviceId)
                    internalRegisteredDevices.remove(device)
                    Option.empty()
                }
            )
        }
    }

    override fun unregisterDeviceResource(resource: DeviceResource): Option<UnregisterError> {
        return when {
            !internalRegisteredDeviceIds.contains(resource.device.deviceId) -> Option.just(
                """
                |Cannot unregister resource ${resource.resourceId} on device
                |${resource.device.deviceId} because device ${resource.device.deviceId} is not
                |registered.
                """.trimMargin()
            )

            !internalRegisteredDeviceResourceIds.containsEntry(
                resource.device.deviceId,
                resource.resourceId
            ) -> Option.just(
                """
                |Cannot unregister resource ${resource.resourceId} on device
                |${resource.device.deviceId} because the resource is not registered on that device.
                """.trimMargin()
            )

            else -> {
                internalRegisteredDeviceResourceIds.remove(
                    resource.device.deviceId,
                    resource.resourceId
                )

                internalRegisteredDeviceResources.remove(resource.device, resource)
                Option.empty()
            }
        }
    }
}
