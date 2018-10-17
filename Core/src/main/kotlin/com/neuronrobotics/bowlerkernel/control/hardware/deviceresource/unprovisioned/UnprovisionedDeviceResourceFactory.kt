package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.google.inject.assistedinject.Assisted
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.neuronrobotics.bowlerkernel.control.hardware.device.Device
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.ResourceId
import com.neuronrobotics.bowlerkernel.control.hardware.registry.HardwareRegistry
import com.neuronrobotics.bowlerkernel.control.hardware.registry.RegisterError
import org.jlleitschuh.guice.module
import javax.inject.Inject

class UnprovisionedDeviceResourceFactory
@Inject internal constructor(
    private val registry: HardwareRegistry,
    @Assisted private val device: Device
) : UnprovisionedLEDFactory {

    private fun registerDeviceResource(resourceId: ResourceId) =
        registry.registerDeviceResource(device.deviceId, resourceId)

    override fun makeUnprovisionedLED(pinNumber: Int): Either<RegisterError, UnprovisionedLED> {
        val resourceId = pinNumber.toString()

        return if (device.isResourceInRange(resourceId)) {
            registerDeviceResource(resourceId).toEither { UnprovisionedLED(device, resourceId) }
                .swap()
        } else {
            Either.left(
                RegisterError(
                    """
                    Could not make unprovisioned LED with resource id $resourceId because it
                    is not in the range of resources for device $device.
                    """.trimIndent()
                )
            )
        }
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
