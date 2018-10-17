package com.neuronrobotics.bowlerkernel.control.hardware.registry

import arrow.core.Option
import com.google.common.collect.MultimapBuilder
import com.google.common.collect.SetMultimap
import com.neuronrobotics.bowlerkernel.control.hardware.device.DeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.ResourceId

internal class StaticHardwareRegistry
internal constructor() : HardwareRegistry {

    private val registeredDevices: MutableSet<DeviceId> = mutableSetOf()

    @Suppress("UnstableApiUsage")
    private val registeredDeviceResources: SetMultimap<DeviceId, ResourceId> =
        MultimapBuilder.hashKeys().hashSetValues().build()

    override fun registerDevice(deviceId: DeviceId): Option<RegisterError> {
        if (registeredDevices.contains(deviceId)) {
            return Option.just(
                RegisterError(
                    """
                    Cannot register device $deviceId because the device is already registered.
                    """.trimIndent()
                )
            )
        }

        registeredDevices.add(deviceId)
        return Option.empty()
    }

    override fun registerDeviceResource(
        deviceId: DeviceId,
        resourceId: ResourceId
    ): Option<RegisterError> {
        if (!registeredDevices.contains(deviceId)) {
            return Option.just(
                RegisterError(
                    """
                    Cannot register resource $resourceId because device $deviceId is not
                    registered.
                    """.trimIndent()
                )
            )
        } else if (registeredDeviceResources.containsEntry(deviceId, resourceId)) {
            return Option.just(
                RegisterError(
                    """
                    Cannot register resource $resourceId on device $deviceId because the
                    resource is already registered.
                    """.trimIndent()
                )
            )
        }

        registeredDeviceResources.put(deviceId, resourceId)
        return Option.empty()
    }

    override fun unregisterDevice(deviceId: DeviceId): Option<UnregisterError> {
        if (!registeredDevices.contains(deviceId)) {
            return Option.just(
                UnregisterError(
                    """
                    Cannot unregister device $deviceId because the device is not registered.
                    """.trimIndent()
                )
            )
        }

        registeredDevices.remove(deviceId)
        return Option.empty()
    }

    override fun unregisterDeviceResource(
        deviceId: DeviceId,
        resourceId: ResourceId
    ): Option<UnregisterError> {
        if (!registeredDevices.contains(deviceId)) {
            return Option.just(
                UnregisterError(
                    """
                    Cannot unregister resource $resourceId on device $deviceId because device
                    $deviceId is not registered.
                    """.trimIndent()
                )
            )
        } else if (!registeredDeviceResources.containsEntry(deviceId, resourceId)) {
            return Option.just(
                UnregisterError(
                    """
                    Cannot unregister resource $resourceId on device $deviceId because the
                    resource is not registered on that device.
                    """.trimIndent()
                )
            )
        }

        registeredDeviceResources.remove(deviceId, resourceId)
        return Option.empty()
    }
}
