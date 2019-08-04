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
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
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
import com.neuronrobotics.bowlerkernel.hardware.registry.error.UnregisterError
import org.octogonapus.ktguava.collections.toImmutableList
import org.octogonapus.ktguava.collections.toImmutableMap
import org.octogonapus.ktguava.collections.toImmutableSet

/**
 * A proxy to keep track of all hardware registered through this class. Meant to be used to
 * unregister any hardware a script might have registered.
 *
 * @param registry The [HardwareRegistry] to proxy and delegate to.
 */
class HardwareRegistryTracker(
    private val registry: HardwareRegistry
) : HardwareRegistry {

    /**
     * The devices that have been registered through this proxy.
     */
    internal val sessionRegisteredDevices: MutableSet<Device> = mutableSetOf()

    /**
     * The device resources that have been registered through this proxy.
     */
    @Suppress("UnstableApiUsage")
    internal val sessionRegisteredDeviceResources: SetMultimap<Device,
        Either<DeviceResource, DeviceResourceGroup>> =
        MultimapBuilder.hashKeys().hashSetValues().build()

    override val registeredDevices
        get() = registry.registeredDevices

    override val registeredDeviceResources
        get() = registry.registeredDeviceResources

    override fun <T : Device> registerDevice(
        deviceId: DeviceId,
        makeDevice: (DeviceId) -> T
    ): Either<RegisterDeviceError, T> {
        val registerError = registry.registerDevice(deviceId, makeDevice)

        registerError.map {
            sessionRegisteredDevices.add(it)
        }

        return registerError
    }

    override fun <D : Device, T : UnprovisionedDeviceResource<*>> registerDeviceResource(
        device: D,
        resourceId: ResourceId,
        makeResource: (D, ResourceId) -> T
    ): Either<RegisterDeviceResourceError, T> {
        val registerError = registry.registerDeviceResource(device, resourceId, makeResource)

        registerError.map {
            sessionRegisteredDeviceResources.put(device, it.left())
        }

        return registerError
    }

    override fun <D : Device, T : UnprovisionedDeviceResourceGroup<*>> registerDeviceResourceGroup(
        device: D,
        resourceIds: ImmutableList<ResourceId>,
        makeResourceGroup: (D, ImmutableList<ResourceId>) -> T
    ): Either<RegisterDeviceResourceGroupError, T> {
        val registerError =
            registry.registerDeviceResourceGroup(device, resourceIds, makeResourceGroup)

        registerError.map {
            sessionRegisteredDeviceResources.put(device, it.right())
        }

        return registerError
    }

    override fun unregisterDevice(device: Device): Option<UnregisterDeviceError> {
        val unregisterError = registry.unregisterDevice(device)

        if (unregisterError.isEmpty()) {
            sessionRegisteredDevices.remove(device)
        }

        return unregisterError
    }

    override fun unregisterDeviceResource(
        resource: DeviceResource
    ): Option<UnregisterDeviceResourceError> {
        val unregisterError = registry.unregisterDeviceResource(resource)

        if (unregisterError.isEmpty()) {
            sessionRegisteredDeviceResources.remove(resource.device, resource.left())
        }

        return unregisterError
    }

    override fun unregisterDeviceResourceGroup(
        resourceGroup: DeviceResourceGroup
    ): Option<UnregisterDeviceResourceGroupError> {
        val unregisterError = registry.unregisterDeviceResourceGroup(resourceGroup)

        if (unregisterError.isEmpty()) {
            sessionRegisteredDeviceResources.remove(resourceGroup.device, resourceGroup.right())
        }

        return unregisterError
    }

    /**
     * Unregisters all hardware registered through this registry.
     *
     * @return A list of all the errors encountered.
     */
    fun unregisterAllHardware(): ImmutableList<UnregisterError> {
        val unregisterDeviceResourceErrors = sessionRegisteredDeviceResources.asMap()
            .mapValues { (device, resources) ->
                device to resources.map { resource ->
                    resource.fold(
                        { registry.unregisterDeviceResource(it) },
                        { registry.unregisterDeviceResourceGroup(it) }
                    )
                }
            }

        unregisterDeviceResourceErrors.filterValues { it.second.all { option -> option is None } }
            .forEach {
                sessionRegisteredDeviceResources.remove(it.key, it.value)
            }

        val unregisterDeviceErrors = sessionRegisteredDevices.map {
            it to registry.unregisterDevice(it)
        }.toImmutableMap()

        unregisterDeviceErrors.filter { it.value is None }.forEach { (device, _) ->
            sessionRegisteredDevices.remove(device)

            // Also be sure to clean up any resources that got stranded
            val strandedResources = sessionRegisteredDeviceResources[device] ?: emptySet()
            removeStrandedResources(device, strandedResources.toImmutableSet())
        }

        return (unregisterDeviceResourceErrors.flatMap {
            it.value.second.mapNotNull { (it as? Some)?.t }
        } + unregisterDeviceErrors.mapNotNull { (it.value as? Some)?.t }).toImmutableList()
    }

    private fun removeStrandedResources(
        device: Device,
        resources: Set<Either<DeviceResource, DeviceResourceGroup>>
    ) {
        if (registry is HardwareRegistryTracker) {
            registry.removeStrandedResources(device, resources)
        }

        resources.forEach { resource ->
            sessionRegisteredDeviceResources.remove(device, resource)
        }
    }

    companion object {

        /**
         * Creates a new [HardwareRegistryTracker] that proxies a [BaseHardwareRegistry].
         */
        fun createWithBaseRegistry() = HardwareRegistryTracker(BaseHardwareRegistry())
    }
}
