package com.neuronrobotics.bowlerkernel.control.hardware.registry

import arrow.core.Option
import com.google.common.collect.ImmutableList
import com.google.common.collect.MultimapBuilder
import com.google.common.collect.SetMultimap
import com.neuronrobotics.bowlerkernel.control.hardware.device.DeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.ResourceId
import com.neuronrobotics.bowlerkernel.util.plus
import com.neuronrobotics.bowlerkernel.util.toImmutableList
import javax.inject.Inject

internal class HardwareRegistryTracker
@Inject internal constructor(
    private val registry: StaticHardwareRegistry
) : HardwareRegistry {

    private val registeredDevices: MutableSet<DeviceId> = mutableSetOf()

    @Suppress("UnstableApiUsage")
    private val registeredDeviceResources: SetMultimap<DeviceId, ResourceId> =
        MultimapBuilder.hashKeys().hashSetValues().build()

    override fun registerDevice(deviceId: DeviceId): Option<RegisterError> {
        val registerError = registry.registerDevice(deviceId)

        if (registerError.isEmpty()) {
            registeredDevices.add(deviceId)
        }

        return registerError
    }

    override fun registerDeviceResource(
        deviceId: DeviceId,
        resourceId: ResourceId
    ): Option<RegisterError> {
        val registerError = registry.registerDeviceResource(deviceId, resourceId)

        if (registerError.isEmpty()) {
            registeredDeviceResources.put(deviceId, resourceId)
        }

        return registerError
    }

    override fun unregisterDevice(deviceId: DeviceId): Option<UnregisterError> {
        val unregisterError = registry.unregisterDevice(deviceId)

        if (unregisterError.isEmpty()) {
            registeredDevices.remove(deviceId)
        }

        return unregisterError
    }

    override fun unregisterDeviceResource(
        deviceId: DeviceId,
        resourceId: ResourceId
    ): Option<UnregisterError> {
        val unregisterError = registry.unregisterDeviceResource(deviceId, resourceId)

        if (unregisterError.isEmpty()) {
            registeredDeviceResources.remove(deviceId, resourceId)
        }

        return unregisterError
    }

    internal fun unregisterAllHardware(): ImmutableList<UnregisterError> {
        val unregisterDeviceResourceErrors = registeredDeviceResources.entries()
            .fold(emptyList<UnregisterError>()) { acc, elem ->
                acc + registry.unregisterDeviceResource(elem.key, elem.value).fold(
                    { emptyList<UnregisterError>() },
                    { listOf(it) }
                )
            }.toImmutableList()

        registeredDeviceResources.clear()

        val unregisterDeviceErrors =
            registeredDevices.fold(emptyList<UnregisterError>()) { acc, elem ->
                acc + registry.unregisterDevice(elem).fold(
                    { emptyList<UnregisterError>() },
                    { listOf(it) }
                )
            }.toImmutableList()

        registeredDevices.clear()

        return unregisterDeviceResourceErrors + unregisterDeviceErrors
    }
}
