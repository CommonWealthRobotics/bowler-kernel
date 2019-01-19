/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.registry

import arrow.core.Either
import arrow.core.Option
import arrow.core.Try
import arrow.core.right
import com.google.common.collect.MultimapBuilder
import com.google.common.collect.SetMultimap
import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.DeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResource
import org.octogonapus.guavautil.collections.toImmutableSet
import org.octogonapus.guavautil.collections.toImmutableSetMultimap

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
        if (internalRegisteredDeviceIds.contains(deviceId)) {
            return Either.left(
                """
                Cannot register device $deviceId because the device is already registered.
                """.trimIndent()
            )
        }

        internalRegisteredDeviceIds.add(deviceId)
        return makeDevice(deviceId).also {
            internalRegisteredDevices.add(it)
        }.right()
    }

    override fun <D : Device, T : UnprovisionedDeviceResource> registerDeviceResource(
        device: D,
        resourceId: ResourceId,
        makeResource: (D, ResourceId) -> T
    ): Either<RegisterError, T> {
        if (!internalRegisteredDeviceIds.contains(device.deviceId)) {
            return Either.left(
                """
                Cannot register resource $resourceId on device ${device.deviceId} because device
                ${device.deviceId} is not registered.
                """.trimIndent()
            )
        } else if (internalRegisteredDeviceResourceIds.containsEntry(device.deviceId, resourceId)) {
            return Either.left(
                """
                Cannot register resource $resourceId on device ${device.deviceId} because the
                resource is already registered.
                """.trimIndent()
            )
        }

        internalRegisteredDeviceResourceIds.put(device.deviceId, resourceId)
        return makeResource(device, resourceId).also {
            internalRegisteredDeviceResources.put(device, it)
        }.right()
    }

    override fun unregisterDevice(device: Device): Option<UnregisterError> {
        if (!internalRegisteredDeviceIds.contains(device.deviceId)) {
            return Option.just(
                """
                Cannot unregister device ${device.deviceId} because the device is not registered.
                """.trimIndent()
            )
        } else if (internalRegisteredDeviceResourceIds[device.deviceId].isNotEmpty()) {
            return Option.just(
                """
                Cannot unregister device ${device.deviceId} because there are registered
                resources attached to it.
                """.trimIndent()
            )
        }

        return Try {
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

    override fun unregisterDeviceResource(resource: DeviceResource): Option<UnregisterError> {
        if (!internalRegisteredDeviceIds.contains(resource.device.deviceId)) {
            return Option.just(
                """
                Cannot unregister resource ${resource.resourceId} on device
                ${resource.device.deviceId} because device ${resource.device.deviceId} is not
                registered.
                """.trimIndent()
            )
        } else if (!internalRegisteredDeviceResourceIds.containsEntry(
                resource.device.deviceId,
                resource.resourceId
            )
        ) {
            return Option.just(
                """
                Cannot unregister resource ${resource.resourceId} on device
                ${resource.device.deviceId} because the resource is not registered on that device.
                """.trimIndent()
            )
        }

        internalRegisteredDeviceResourceIds.remove(resource.device.deviceId, resource.resourceId)
        internalRegisteredDeviceResources.remove(resource.device, resource)
        return Option.empty()
    }
}
