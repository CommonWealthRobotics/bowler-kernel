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
import arrow.core.left
import arrow.core.right
import com.google.common.collect.ImmutableList
import com.google.common.collect.MultimapBuilder
import com.google.common.collect.SetMultimap
import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.DeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.DeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.registry.error.RegisterDeviceError
import com.neuronrobotics.bowlerkernel.hardware.registry.error.RegisterDeviceResourceError
import com.neuronrobotics.bowlerkernel.hardware.registry.error.RegisterDeviceResourceGroupError
import com.neuronrobotics.bowlerkernel.hardware.registry.error.UnregisterDeviceError
import com.neuronrobotics.bowlerkernel.hardware.registry.error.UnregisterDeviceResourceError
import com.neuronrobotics.bowlerkernel.hardware.registry.error.UnregisterDeviceResourceGroupError
import mu.KotlinLogging
import org.octogonapus.ktguava.collections.toImmutableSet
import org.octogonapus.ktguava.collections.toImmutableSetMultimap

/**
 * The base implementation of [HardwareRegistry]. This class should always be wrapped with a
 * [HardwareRegistryTracker].
 */
internal class BaseHardwareRegistry : HardwareRegistry {

    private val internalRegisteredDeviceIds: MutableSet<DeviceId> = mutableSetOf()
    private val internalRegisteredDevices: MutableSet<Device> = mutableSetOf()

    @Suppress("UnstableApiUsage")
    private val internalRegisteredDeviceResourceIds: SetMultimap<DeviceId, ResourceId> =
        MultimapBuilder.hashKeys().hashSetValues().build()
    @Suppress("UnstableApiUsage")
    private val internalRegisteredDeviceResources: SetMultimap<Device,
        Either<DeviceResource, DeviceResourceGroup>> =
        MultimapBuilder.hashKeys().hashSetValues().build()

    override val registeredDevices
        get() = internalRegisteredDevices.toImmutableSet()

    override val registeredDeviceResources
        get() = internalRegisteredDeviceResources.toImmutableSetMultimap()

    override fun <T : Device> registerDevice(
        deviceId: DeviceId,
        makeDevice: (DeviceId) -> T
    ): Either<RegisterDeviceError, T> {
        return when {
            internalRegisteredDeviceIds.contains(deviceId) ->
                RegisterDeviceError.DeviceIsAlreadyRegisteredError(deviceId).left()

            else -> {
                LOGGER.debug { "Registering device $deviceId" }

                internalRegisteredDeviceIds.add(deviceId)

                makeDevice(deviceId).also {
                    internalRegisteredDevices.add(it)
                }.right()
            }
        }
    }

    override fun <D : Device, T : UnprovisionedDeviceResource<*>> registerDeviceResource(
        device: D,
        resourceId: ResourceId,
        makeResource: (D, ResourceId) -> T
    ): Either<RegisterDeviceResourceError, T> {
        return when {
            !internalRegisteredDeviceIds.contains(device.deviceId) ->
                RegisterDeviceResourceError.DeviceIsNotRegisteredError(
                    resourceId,
                    device.deviceId
                ).left()

            internalRegisteredDeviceResourceIds.containsEntry(
                device.deviceId,
                resourceId
            ) ->
                RegisterDeviceResourceError.ResourceIsAlreadyRegisteredError(resourceId).left()

            internalRegisteredDeviceResourceIds.values().any {
                it.attachmentPoint == resourceId.attachmentPoint
            } ->
                RegisterDeviceResourceError.ResourceOnSameAttachmentPointError(resourceId).left()

            else -> {
                LOGGER.debug { "Registering resource $resourceId on device $device" }

                internalRegisteredDeviceResourceIds.put(device.deviceId, resourceId)

                makeResource(device, resourceId).also {
                    internalRegisteredDeviceResources.put(device, it.left())
                }.right()
            }
        }
    }

    override fun <D : Device, T : UnprovisionedDeviceResourceGroup<*>> registerDeviceResourceGroup(
        device: D,
        resourceIds: ImmutableList<ResourceId>,
        makeResourceGroup: (D, ImmutableList<ResourceId>) -> T
    ): Either<RegisterDeviceResourceGroupError, T> {
        return when {
            !internalRegisteredDeviceIds.contains(device.deviceId) ->
                RegisterDeviceResourceGroupError.DeviceIsNotRegisteredError(
                    resourceIds,
                    device.deviceId
                ).left()

            resourceIds.any {
                internalRegisteredDeviceResourceIds.containsEntry(device.deviceId, it)
            } -> RegisterDeviceResourceGroupError.ResourceGroupIsAlreadyRegisteredError(
                resourceIds
            ).left()

            resourceIds.any {
                internalRegisteredDeviceResourceIds.values().any { registeredResourceId ->
                    registeredResourceId.attachmentPoint == it.attachmentPoint
                }
            } -> RegisterDeviceResourceGroupError.ResourceOnSameAttachmentPointError(
                resourceIds
            ).left()

            else -> {
                LOGGER.debug {
                    "Registering resource group ${resourceIds.joinToString()} on device $device"
                }

                internalRegisteredDeviceResourceIds.putAll(device.deviceId, resourceIds)

                makeResourceGroup(device, resourceIds).also {
                    internalRegisteredDeviceResources.put(device, it.right())
                }.right()
            }
        }
    }

    override fun unregisterDevice(device: Device): Option<UnregisterDeviceError> {
        return when {
            !internalRegisteredDeviceIds.contains(device.deviceId) ->
                Option.just(UnregisterDeviceError.DeviceIsNotRegisteredError(device.deviceId))

            internalRegisteredDeviceResourceIds[device.deviceId].isNotEmpty() ->
                Option.just(
                    UnregisterDeviceError.DeviceHasRegisteredResourcesError(
                        device.deviceId,
                        internalRegisteredDeviceResourceIds[device.deviceId].toSet()
                    )
                )

            else -> {
                LOGGER.debug { "Unregistering device $device" }

                device.disconnect().fold(
                    {
                        Option.just(UnregisterDeviceError.DisconnectError(it))
                    },
                    {
                        internalRegisteredDeviceIds.remove(device.deviceId)
                        internalRegisteredDevices.remove(device)
                        Option.empty()
                    }
                )
            }
        }
    }

    override fun unregisterDeviceResource(
        resource: DeviceResource
    ): Option<UnregisterDeviceResourceError> {
        return when {
            !internalRegisteredDeviceIds.contains(resource.device.deviceId) -> Option.just(
                UnregisterDeviceResourceError.DeviceIsNotRegisteredError(resource.resourceId)
            )

            !internalRegisteredDeviceResourceIds.containsEntry(
                resource.device.deviceId,
                resource.resourceId
            ) -> Option.just(
                UnregisterDeviceResourceError.ResourceIsNotRegisteredOnDeviceError(
                    resource.resourceId,
                    resource.device.deviceId
                )
            )

            else -> {
                LOGGER.debug { "Unregistering resource $resource" }

                internalRegisteredDeviceResourceIds.remove(
                    resource.device.deviceId,
                    resource.resourceId
                )

                internalRegisteredDeviceResources.remove(resource.device, resource.left())
                Option.empty()
            }
        }
    }

    override fun unregisterDeviceResourceGroup(
        resourceGroup: DeviceResourceGroup
    ): Option<UnregisterDeviceResourceGroupError> {
        return when {
            !internalRegisteredDeviceIds.contains(resourceGroup.device.deviceId) -> Option.just(
                UnregisterDeviceResourceGroupError.DeviceIsNotRegisteredError(resourceGroup.resourceIds)
            )

            resourceGroup.resourceIds.any {
                !internalRegisteredDeviceResourceIds.containsEntry(
                    resourceGroup.device.deviceId,
                    it
                )
            } -> Option.just(
                UnregisterDeviceResourceGroupError.ResourceGroupIsNotRegisteredOnDeviceError(
                    resourceGroup.resourceIds,
                    resourceGroup.device.deviceId
                )
            )

            else -> {
                LOGGER.debug { "Unregistering resource group $resourceGroup" }

                resourceGroup.resourceIds.forEach { id ->
                    internalRegisteredDeviceResourceIds.remove(resourceGroup.device.deviceId, id)
                }

                internalRegisteredDeviceResources.remove(
                    resourceGroup.device,
                    resourceGroup.right()
                )
                Option.empty()
            }
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
