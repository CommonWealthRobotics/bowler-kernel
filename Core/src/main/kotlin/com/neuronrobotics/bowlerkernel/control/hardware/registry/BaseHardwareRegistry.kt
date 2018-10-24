package com.neuronrobotics.bowlerkernel.control.hardware.registry

import arrow.core.Option
import com.google.common.collect.MultimapBuilder
import com.google.common.collect.SetMultimap
import com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.util.toImmutableSet
import com.neuronrobotics.bowlerkernel.util.toImmutableSetMultimap

/**
 * The base implementation of [HardwareRegistry].
 */
internal class BaseHardwareRegistry
internal constructor() : HardwareRegistry {

    private val internalRegisteredDevices: MutableSet<DeviceId> = mutableSetOf()

    @Suppress("UnstableApiUsage")
    private val internalRegisteredDeviceResources: SetMultimap<DeviceId, ResourceId> =
        MultimapBuilder.hashKeys().hashSetValues().build()

    override val registeredDevices
        get() = internalRegisteredDevices.toImmutableSet()

    override val registeredDeviceResources
        get() = internalRegisteredDeviceResources.toImmutableSetMultimap()

    override fun registerDevice(deviceId: DeviceId): Option<RegisterError> {
        if (internalRegisteredDevices.contains(deviceId)) {
            return Option.just(
                RegisterError(
                    """
                    Cannot register device $deviceId because the device is already registered.
                    """.trimIndent()
                )
            )
        }

        internalRegisteredDevices.add(deviceId)
        return Option.empty()
    }

    override fun registerDeviceResource(
        deviceId: DeviceId,
        resourceId: ResourceId
    ): Option<RegisterError> {
        if (!internalRegisteredDevices.contains(deviceId)) {
            return Option.just(
                RegisterError(
                    """
                    Cannot register resource $resourceId because device $deviceId is not
                    registered.
                    """.trimIndent()
                )
            )
        } else if (internalRegisteredDeviceResources.containsEntry(deviceId, resourceId)) {
            return Option.just(
                RegisterError(
                    """
                    Cannot register resource $resourceId on device $deviceId because the
                    resource is already registered.
                    """.trimIndent()
                )
            )
        }

        internalRegisteredDeviceResources.put(deviceId, resourceId)
        return Option.empty()
    }

    override fun unregisterDevice(deviceId: DeviceId): Option<UnregisterError> {
        if (!internalRegisteredDevices.contains(deviceId)) {
            return Option.just(
                UnregisterError(
                    """
                    Cannot unregister device $deviceId because the device is not registered.
                    """.trimIndent()
                )
            )
        }

        internalRegisteredDevices.remove(deviceId)
        return Option.empty()
    }

    override fun unregisterDeviceResource(
        deviceId: DeviceId,
        resourceId: ResourceId
    ): Option<UnregisterError> {
        if (!internalRegisteredDevices.contains(deviceId)) {
            return Option.just(
                UnregisterError(
                    """
                    Cannot unregister resource $resourceId on device $deviceId because device
                    $deviceId is not registered.
                    """.trimIndent()
                )
            )
        } else if (!internalRegisteredDeviceResources.containsEntry(deviceId, resourceId)) {
            return Option.just(
                UnregisterError(
                    """
                    Cannot unregister resource $resourceId on device $deviceId because the
                    resource is not registered on that device.
                    """.trimIndent()
                )
            )
        }

        internalRegisteredDeviceResources.remove(deviceId, resourceId)
        return Option.empty()
    }
}
