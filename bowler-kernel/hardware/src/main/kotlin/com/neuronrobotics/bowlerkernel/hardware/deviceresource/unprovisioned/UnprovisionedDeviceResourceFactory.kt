/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.google.inject.assistedinject.Assisted
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.AttachmentPoint
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.registry.HardwareRegistry
import com.neuronrobotics.bowlerkernel.hardware.registry.RegisterError
import org.jlleitschuh.guice.module
import javax.inject.Inject

/**
 * A facade for making any type of device resource.
 */
@SuppressWarnings("TooManyFunctions")
class UnprovisionedDeviceResourceFactory
@Inject internal constructor(
    private val registry: HardwareRegistry,
    @Assisted private val device: BowlerDevice
) : UnprovisionedAnalogInFactory,
    UnprovisionedAnalogOutFactory,
    UnprovisionedButtonFactory,
    UnprovisionedDigitalInFactory,
    UnprovisionedDigitalOutFactory,
    UnprovisionedEncoderFactory,
    UnprovisionedPiezoelectricSpeakerFactory,
    UnprovisionedSerialConnectionFactory,
    UnprovisionedServoFactory,
    UnprovisionedStepperFactory,
    UnprovisionedUltrasonicFactory {

    private fun <T : UnprovisionedDeviceResource> makeUnprovisionedResource(
        resourceId: ResourceId,
        errorMessageType: String,
        rightSide: (BowlerDevice, ResourceId) -> T
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

    override fun makeUnprovisionedAnalogIn(
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedAnalogIn> =
        makeUnprovisionedResource(
            ResourceId(DefaultResourceTypes.AnalogIn, attachmentPoint),
            "AnalogIn"
        ) { device, resourceId ->
            UnprovisionedAnalogIn(device, resourceId)
        }

    override fun makeUnprovisionedAnalogOut(
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedAnalogOut> =
        makeUnprovisionedResource(
            ResourceId(DefaultResourceTypes.AnalogOut, attachmentPoint),
            "AnalogOut"
        ) { device, resourceId ->
            UnprovisionedAnalogOut(device, resourceId)
        }

    override fun makeUnprovisionedButton(
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedButton> =
        makeUnprovisionedResource(
            ResourceId(DefaultResourceTypes.Button, attachmentPoint),
            "Button"
        ) { device, resourceId ->
            UnprovisionedButton(device, resourceId)
        }

    override fun makeUnprovisionedDigitalIn(
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedDigitalIn> =
        makeUnprovisionedResource(
            ResourceId(DefaultResourceTypes.DigitalIn, attachmentPoint),
            "DigitalIn"
        ) { device, resourceId ->
            UnprovisionedDigitalIn(device, resourceId)
        }

    override fun makeUnprovisionedDigitalOut(
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedDigitalOut> =
        makeUnprovisionedResource(
            ResourceId(DefaultResourceTypes.DigitalOut, attachmentPoint),
            "DigitalOut"
        ) { device, resourceId ->
            UnprovisionedDigitalOut(device, resourceId)
        }

    override fun makeUnprovisionedEncoder(
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedEncoder> =
        makeUnprovisionedResource(
            ResourceId(DefaultResourceTypes.Encoder, attachmentPoint),
            "Encoder"
        ) { device, resourceId ->
            UnprovisionedEncoder(device, resourceId)
        }

    override fun makeUnprovisionedPiezoelectricSpeaker(
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedPiezoelectricSpeaker> =
        makeUnprovisionedResource(
            ResourceId(DefaultResourceTypes.PiezoelectricSpeaker, attachmentPoint),
            "PiezoelectricSpeaker"
        ) { device, resourceId ->
            UnprovisionedPiezoelectricSpeaker(device, resourceId)
        }

    override fun makeUnprovisionedSerialConnection(
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedSerialConnection> =
        makeUnprovisionedResource(
            ResourceId(DefaultResourceTypes.SerialConnection, attachmentPoint),
            "SerialConnection"
        ) { device, resourceId ->
            UnprovisionedSerialConnection(device, resourceId)
        }

    override fun makeUnprovisionedServo(
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedServo> =
        makeUnprovisionedResource(
            ResourceId(DefaultResourceTypes.Servo, attachmentPoint),
            "Servo"
        ) { device, resourceId ->
            UnprovisionedServo(device, resourceId)
        }

    override fun makeUnprovisionedStepper(
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedStepper> =
        makeUnprovisionedResource(
            ResourceId(DefaultResourceTypes.Stepper, attachmentPoint),
            "Stepper"
        ) { device, resourceId ->
            UnprovisionedStepper(device, resourceId)
        }

    override fun makeUnprovisionedUltrasonic(
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedUltrasonic> =
        makeUnprovisionedResource(
            ResourceId(DefaultResourceTypes.Ultrasonic, attachmentPoint),
            DefaultResourceTypes.Ultrasonic::class.java.name
        ) { device, resourceId ->
            UnprovisionedUltrasonic(device, resourceId)
        }

    companion object {
        internal fun unprovisionedDeviceResourceFactoryModule() = module {
            install(
                FactoryModuleBuilder()
                    .implement(
                        UnprovisionedAnalogInFactory::class.java,
                        UnprovisionedDeviceResourceFactory::class.java
                    ).build(
                        UnprovisionedAnalogInFactory.Factory::class.java
                    )
            )

            install(
                FactoryModuleBuilder()
                    .implement(
                        UnprovisionedAnalogOutFactory::class.java,
                        UnprovisionedDeviceResourceFactory::class.java
                    ).build(
                        UnprovisionedAnalogOutFactory.Factory::class.java
                    )
            )

            install(
                FactoryModuleBuilder()
                    .implement(
                        UnprovisionedButtonFactory::class.java,
                        UnprovisionedDeviceResourceFactory::class.java
                    ).build(
                        UnprovisionedButtonFactory.Factory::class.java
                    )
            )

            install(
                FactoryModuleBuilder()
                    .implement(
                        UnprovisionedDigitalInFactory::class.java,
                        UnprovisionedDeviceResourceFactory::class.java
                    ).build(
                        UnprovisionedDigitalInFactory.Factory::class.java
                    )
            )

            install(
                FactoryModuleBuilder()
                    .implement(
                        UnprovisionedDigitalOutFactory::class.java,
                        UnprovisionedDeviceResourceFactory::class.java
                    ).build(
                        UnprovisionedDigitalOutFactory.Factory::class.java
                    )
            )

            install(
                FactoryModuleBuilder()
                    .implement(
                        UnprovisionedEncoderFactory::class.java,
                        UnprovisionedDeviceResourceFactory::class.java
                    ).build(
                        UnprovisionedEncoderFactory.Factory::class.java
                    )
            )

            install(
                FactoryModuleBuilder()
                    .implement(
                        UnprovisionedPiezoelectricSpeakerFactory::class.java,
                        UnprovisionedDeviceResourceFactory::class.java
                    ).build(
                        UnprovisionedPiezoelectricSpeakerFactory.Factory::class.java
                    )
            )

            install(
                FactoryModuleBuilder()
                    .implement(
                        UnprovisionedSerialConnectionFactory::class.java,
                        UnprovisionedDeviceResourceFactory::class.java
                    ).build(
                        UnprovisionedSerialConnectionFactory.Factory::class.java
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

            install(
                FactoryModuleBuilder()
                    .implement(
                        UnprovisionedStepperFactory::class.java,
                        UnprovisionedDeviceResourceFactory::class.java
                    ).build(
                        UnprovisionedStepperFactory.Factory::class.java
                    )
            )

            install(
                FactoryModuleBuilder()
                    .implement(
                        UnprovisionedUltrasonicFactory::class.java,
                        UnprovisionedDeviceResourceFactory::class.java
                    ).build(
                        UnprovisionedUltrasonicFactory.Factory::class.java
                    )
            )
        }
    }
}
