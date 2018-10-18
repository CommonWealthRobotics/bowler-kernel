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

/**
 * A proxy to keep track of all hardware registered through this class. Meant to be used to
 * unregister any hardware a control script might have registered.
 *
 * @param registry The [StaticHardwareRegistry] to proxy and delegate to.
 */
internal class HardwareRegistryTracker
@Inject internal constructor(
    private val registry: StaticHardwareRegistry
) : HardwareRegistry {

    /**
     * The devices that have been registered through this proxy.
     */
    internal val sessionRegisteredDevices: MutableSet<DeviceId> = mutableSetOf()

    /**
     * The device resources that have been registered through this proxy.
     */
    @Suppress("UnstableApiUsage")
    internal val sessionRegisteredDeviceResources: SetMultimap<DeviceId, ResourceId> =
        MultimapBuilder.hashKeys().hashSetValues().build()

    override val registeredDevices
        get() = registry.registeredDevices

    override val registeredDeviceResources
        get() = registry.registeredDeviceResources

    override fun registerDevice(deviceId: DeviceId): Option<RegisterError> {
        val registerError = registry.registerDevice(deviceId)

        if (registerError.isEmpty()) {
            sessionRegisteredDevices.add(deviceId)
        }

        return registerError
    }

    override fun registerDeviceResource(
        deviceId: DeviceId,
        resourceId: ResourceId
    ): Option<RegisterError> {
        val registerError = registry.registerDeviceResource(deviceId, resourceId)

        if (registerError.isEmpty()) {
            sessionRegisteredDeviceResources.put(deviceId, resourceId)
        }

        return registerError
    }

    override fun unregisterDevice(deviceId: DeviceId): Option<UnregisterError> {
        val unregisterError = registry.unregisterDevice(deviceId)

        if (unregisterError.isEmpty()) {
            sessionRegisteredDevices.remove(deviceId)
        }

        return unregisterError
    }

    override fun unregisterDeviceResource(
        deviceId: DeviceId,
        resourceId: ResourceId
    ): Option<UnregisterError> {
        val unregisterError = registry.unregisterDeviceResource(deviceId, resourceId)

        if (unregisterError.isEmpty()) {
            sessionRegisteredDeviceResources.remove(deviceId, resourceId)
        }

        return unregisterError
    }

    /**
     * Unregisters all hardware registered through this registry.
     *
     * @return A list of all the errors encountered.
     */
    internal fun unregisterAllHardware(): ImmutableList<UnregisterError> {
        val unregisterDeviceResourceErrors = sessionRegisteredDeviceResources.entries()
            .fold(emptyList<UnregisterError>()) { acc, elem ->
                acc + registry.unregisterDeviceResource(elem.key, elem.value).fold(
                    { emptyList<UnregisterError>() },
                    { listOf(it) }
                )
            }.toImmutableList()

        sessionRegisteredDeviceResources.clear()

        val unregisterDeviceErrors =
            sessionRegisteredDevices.fold(emptyList<UnregisterError>()) { acc, elem ->
                acc + registry.unregisterDevice(elem).fold(
                    { emptyList<UnregisterError>() },
                    { listOf(it) }
                )
            }.toImmutableList()

        sessionRegisteredDevices.clear()

        return unregisterDeviceResourceErrors + unregisterDeviceErrors
    }
}
