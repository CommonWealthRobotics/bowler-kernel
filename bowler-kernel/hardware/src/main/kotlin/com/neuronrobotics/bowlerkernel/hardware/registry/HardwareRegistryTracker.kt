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
import com.google.common.collect.ImmutableList
import com.google.common.collect.MultimapBuilder
import com.google.common.collect.SetMultimap
import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.DeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResource
import org.octogonapus.guavautil.collections.toImmutableList
import org.octogonapus.guavautil.collections.plus
import javax.inject.Inject

/**
 * A proxy to keep track of all hardware registered through this class. Meant to be used to
 * unregister any hardware a script might have registered.
 *
 * @param registry The [BaseHardwareRegistry] to proxy and delegate to.
 */
internal class HardwareRegistryTracker
@Inject internal constructor(
    private val registry: BaseHardwareRegistry
) : HardwareRegistry {

    /**
     * The devices that have been registered through this proxy.
     */
    internal val sessionRegisteredDevices: MutableSet<Device> = mutableSetOf()

    /**
     * The device resources that have been registered through this proxy.
     */
    @Suppress("UnstableApiUsage")
    internal val sessionRegisteredDeviceResources: SetMultimap<Device, DeviceResource> =
        MultimapBuilder.hashKeys().hashSetValues().build()

    override val registeredDevices
        get() = registry.registeredDevices

    override val registeredDeviceResources
        get() = registry.registeredDeviceResources

    override fun <T : Device> registerDevice(
        deviceId: DeviceId,
        makeDevice: (DeviceId) -> T
    ): Either<RegisterError, T> {
        val registerError = registry.registerDevice(deviceId, makeDevice)

        registerError.map {
            sessionRegisteredDevices.add(it)
        }

        return registerError
    }

    override fun <D : Device, T : UnprovisionedDeviceResource> registerDeviceResource(
        device: D,
        resourceId: ResourceId,
        makeResource: (D, ResourceId) -> T
    ): Either<RegisterError, T> {
        val registerError = registry.registerDeviceResource(device, resourceId, makeResource)

        registerError.map {
            sessionRegisteredDeviceResources.put(device, it)
        }

        return registerError
    }

    override fun unregisterDevice(device: Device): Option<UnregisterError> {
        val unregisterError = registry.unregisterDevice(device)

        if (unregisterError.isEmpty()) {
            sessionRegisteredDevices.remove(device)
        }

        return unregisterError
    }

    override fun unregisterDeviceResource(resource: DeviceResource): Option<UnregisterError> {
        val unregisterError = registry.unregisterDeviceResource(resource)

        if (unregisterError.isEmpty()) {
            sessionRegisteredDeviceResources.remove(resource.device, resource)
        }

        return unregisterError
    }

    /**
     * Unregisters all hardware registered through this registry.
     *
     * @return A list of all the errors encountered.
     */
    internal fun unregisterAllHardware(): ImmutableList<UnregisterError> {
        val unregisterDeviceResourceErrors = sessionRegisteredDeviceResources.entries().fold(
            mapOf<Option<UnregisterError>, Map.Entry<Device, DeviceResource>?>(
                Option.empty<UnregisterError>() to null
            )
        ) { acc, elem ->
            acc + mapOf(
                registry.unregisterDeviceResource(elem.value).fold(
                    { Option.empty<UnregisterError>() to elem },
                    { Option.just(it) to null }
                )
            )
        }.toMap()

        // Can't call removeAll() because it doesn't mutate a Multimap
        unregisterDeviceResourceErrors.mapNotNull { it.value }.forEach {
            sessionRegisteredDeviceResources.remove(it.key, it.value)
        }

        val unregisterDeviceErrors = sessionRegisteredDevices.fold(
            mapOf<Option<UnregisterError>, Device?>(Option.empty<UnregisterError>() to null)
        ) { acc, elem ->
            acc + mapOf(
                registry.unregisterDevice(elem).fold(
                    { Option.empty<UnregisterError>() to elem },
                    { Option.just(it) to null }
                )
            )
        }.toMap()

        unregisterDeviceErrors.mapNotNull { it.value }.let {
            sessionRegisteredDevices.removeAll(it)

            // Also be sure to clean up any resources that got stranded
            sessionRegisteredDeviceResources.removeAll(it)
        }

        return unregisterDeviceResourceErrors.mapNotNull {
            it.key.fold({ null }, { it })
        }.toImmutableList() + unregisterDeviceErrors.mapNotNull {
            it.key.fold({ null }, { it })
        }.toImmutableList()
    }
}
