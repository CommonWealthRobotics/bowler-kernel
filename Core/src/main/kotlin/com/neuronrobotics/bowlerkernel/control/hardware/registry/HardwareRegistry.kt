package com.neuronrobotics.bowlerkernel.control.hardware.registry

import arrow.core.Option
import com.neuronrobotics.bowlerkernel.control.hardware.device.DeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.ResourceId

internal interface HardwareRegistry {

    /**
     * Registers a device id. Fails if the device is already registered.
     *
     * @param deviceId The device id to register.
     * @return An empty option on success, a [RegisterError] on failure.
     */
    fun registerDevice(deviceId: DeviceId): Option<RegisterError>

    /**
     * Registers a resource id attached (physically) to a device. Fails if the resource is
     * already registered or if the device is not registered.
     *
     * @param deviceId The device the resource is attached to.
     * @param resourceId The resource id to register.
     * @return An empty option on success, a [RegisterError] on failure.
     */
    fun registerDeviceResource(deviceId: DeviceId, resourceId: ResourceId): Option<RegisterError>

    /**
     * Unregisters a device id. Fails if the device is not registered.
     *
     * @param deviceId The device id to unregister.
     * @return An empty option on success, an [UnregisterError] on failure.
     */
    fun unregisterDevice(deviceId: DeviceId): Option<UnregisterError>

    /**
     * Unregisters a resource id attached (physically) to a device. Fails if the resource is
     * not registered or if the device is not registered.
     *
     * @param deviceId The device the resource is attached to.
     * @param resourceId The resource id to unregister.
     * @return An empty option on success, an [UnregisterError] on failure.
     */
    fun unregisterDeviceResource(
        deviceId: DeviceId,
        resourceId: ResourceId
    ): Option<UnregisterError>
}
