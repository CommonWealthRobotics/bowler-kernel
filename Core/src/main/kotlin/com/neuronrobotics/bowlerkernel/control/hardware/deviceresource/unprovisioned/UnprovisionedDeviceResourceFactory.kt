/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.google.inject.assistedinject.Assisted
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.neuronrobotics.bowlerkernel.control.hardware.device.Device
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.PinNumber
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.control.hardware.registry.HardwareRegistry
import com.neuronrobotics.bowlerkernel.control.hardware.registry.RegisterError
import org.jlleitschuh.guice.module
import javax.inject.Inject

/**
 * A facade for making any type of device resource.
 */
class UnprovisionedDeviceResourceFactory
@Inject internal constructor(
    private val registry: HardwareRegistry,
    @Assisted private val device: Device
) : UnprovisionedLEDFactory {

    private fun registerDeviceResource(resourceId: ResourceId) =
        registry.registerDeviceResource(device.deviceId, resourceId)

    private inline fun <T> makeUnprovisionedResource(
        resourceId: ResourceId,
        errorMessageType: String,
        crossinline rightSide: (ResourceId) -> T
    ):
        Either<RegisterError, T> {
        return if (device.isResourceInRange(resourceId)) {
            registerDeviceResource(resourceId).toEither { rightSide(resourceId) }
                .swap()
        } else {
            Either.left(
                RegisterError(
                    """
                    Could not make an unprovisioned $errorMessageType with resource id
                    $resourceId because it is not in the range of resources for device $device.
                    """.trimIndent()
                )
            )
        }
    }

    override fun makeUnprovisionedLED(pinNumber: PinNumber): Either<RegisterError, UnprovisionedLED> =
        makeUnprovisionedResource(pinNumber, "LED") {
            UnprovisionedLED(device, it)
        }

    companion object {

        internal fun unprovisionedDeviceResourceFactoryModule() = module {
            install(
                FactoryModuleBuilder()
                    .implement(
                        UnprovisionedLEDFactory::class.java,
                        UnprovisionedDeviceResourceFactory::class.java
                    ).build(
                        UnprovisionedLEDFactory.Factory::class.java
                    )
            )
        }
    }
}
