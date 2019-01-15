/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.google.inject.assistedinject.Assisted
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.registry.HardwareRegistry
import com.neuronrobotics.bowlerkernel.hardware.registry.RegisterError
import org.jlleitschuh.guice.module
import javax.inject.Inject

/**
 * A facade for making any type of device resource.
 */
class UnprovisionedDeviceResourceFactory
@Inject internal constructor(
    private val registry: HardwareRegistry,
    @Assisted private val device: BowlerDevice
) : UnprovisionedLEDFactory, UnprovisionedServoFactory {

    private inline fun <T : UnprovisionedDeviceResource> makeUnprovisionedResource(
        resourceId: ResourceId,
        errorMessageType: String,
        crossinline rightSide: (BowlerDevice, ResourceId) -> T
    ):
        Either<RegisterError, T> {
        return if (device.isResourceInRange(resourceId)) {
            registry.registerDeviceResource(device, resourceId) { device, resource ->
                rightSide(device, resource)
            }
        } else {
            Either.left(
                """
                Could not make an unprovisioned $errorMessageType with resource id
                $resourceId because it is not in the range of resources for device $device.
                """.trimIndent()
            )
        }
    }

    override fun makeUnprovisionedLED(resourceId: ResourceId) =
        makeUnprovisionedResource(resourceId, "LED") { device, resourceId ->
            UnprovisionedLED(device, resourceId)
        }

    override fun makeUnprovisionedServo(resourceId: ResourceId) =
        makeUnprovisionedResource(resourceId, "Servo") { device, resourceId ->
            UnprovisionedServo(device, resourceId)
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
            install(
                FactoryModuleBuilder()
                    .implement(
                        UnprovisionedServoFactory::class.java,
                        UnprovisionedDeviceResourceFactory::class.java
                    ).build(
                        UnprovisionedServoFactory.Factory::class.java
                    )
            )
        }
    }
}
