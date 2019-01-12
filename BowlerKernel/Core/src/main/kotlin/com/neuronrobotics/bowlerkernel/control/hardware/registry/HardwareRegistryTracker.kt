/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.registry

import arrow.core.Either
import arrow.core.Option
import com.google.common.collect.ImmutableList
import com.google.common.collect.MultimapBuilder
import com.google.common.collect.SetMultimap
import com.neuronrobotics.bowlerkernel.control.hardware.device.Device
import com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.DeviceResource
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResource
import com.neuronrobotics.bowlerkernel.util.plus
import com.neuronrobotics.bowlerkernel.util.toImmutableList
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
