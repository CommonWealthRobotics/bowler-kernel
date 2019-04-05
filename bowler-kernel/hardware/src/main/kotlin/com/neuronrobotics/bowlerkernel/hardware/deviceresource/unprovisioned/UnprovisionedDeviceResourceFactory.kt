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
import arrow.core.left
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.AttachmentPoint
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.registry.HardwareRegistry
import com.neuronrobotics.bowlerkernel.hardware.registry.RegisterError
import org.jlleitschuh.guice.module
import javax.inject.Inject

/**
 * A facade for making any type of device resource. Requires the [device] to be connected or else
 * require creation will fail due to RPC timeout.
 */
@SuppressWarnings("TooManyFunctions")
class UnprovisionedDeviceResourceFactory
@Inject internal constructor(
    private val registry: HardwareRegistry
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
        device: BowlerDevice,
        resourceId: ResourceId,
        errorMessageType: String,
        makeResource: (BowlerDevice, ResourceId) -> T
    ):
        Either<RegisterError, T> {
        return if (device.isResourceInRange(resourceId)) {
            registry.registerDeviceResource(device, resourceId, makeResource)
        } else {
            """
            Could not make an unprovisioned $errorMessageType with resource id
            $resourceId because it is not in the range of resources for device $device.
            """.trimIndent().left()
        }
    }

    override fun makeUnprovisionedAnalogIn(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedAnalogIn> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.AnalogIn, attachmentPoint),
            "AnalogIn"
        ) { registeredDevice, resourceId ->
            UnprovisionedAnalogIn(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedAnalogOut(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedAnalogOut> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.AnalogOut, attachmentPoint),
            "AnalogOut"
        ) { registeredDevice, resourceId ->
            UnprovisionedAnalogOut(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedButton(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedButton> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.Button, attachmentPoint),
            "Button"
        ) { registeredDevice, resourceId ->
            UnprovisionedButton(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedDigitalIn(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedDigitalIn> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.DigitalIn, attachmentPoint),
            "DigitalIn"
        ) { registeredDevice, resourceId ->
            UnprovisionedDigitalIn(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedDigitalOut(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedDigitalOut> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.DigitalOut, attachmentPoint),
            "DigitalOut"
        ) { registeredDevice, resourceId ->
            UnprovisionedDigitalOut(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedEncoder(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedEncoder> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.Encoder, attachmentPoint),
            "Encoder"
        ) { registeredDevice, resourceId ->
            UnprovisionedEncoder(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedPiezoelectricSpeaker(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedPiezoelectricSpeaker> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.PiezoelectricSpeaker, attachmentPoint),
            "PiezoelectricSpeaker"
        ) { registeredDevice, resourceId ->
            UnprovisionedPiezoelectricSpeaker(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedSerialConnection(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedSerialConnection> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.SerialConnection, attachmentPoint),
            "SerialConnection"
        ) { registeredDevice, resourceId ->
            UnprovisionedSerialConnection(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedServo(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedServo> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.Servo, attachmentPoint),
            "Servo"
        ) { registeredDevice, resourceId ->
            UnprovisionedServo(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedStepper(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedStepper> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.Stepper, attachmentPoint),
            "Stepper"
        ) { registeredDevice, resourceId ->
            UnprovisionedStepper(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedUltrasonic(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedUltrasonic> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.Ultrasonic, attachmentPoint),
            "Ultrasonic"
        ) { registeredDevice, resourceId ->
            UnprovisionedUltrasonic(registeredDevice, resourceId)
        }

    companion object {

        fun unprovisionedDeviceResourceFactoryModule() = module {
            bind<UnprovisionedAnalogInFactory>().to<UnprovisionedDeviceResourceFactory>()
            bind<UnprovisionedAnalogOutFactory>().to<UnprovisionedDeviceResourceFactory>()
            bind<UnprovisionedButtonFactory>().to<UnprovisionedDeviceResourceFactory>()
            bind<UnprovisionedDigitalInFactory>().to<UnprovisionedDeviceResourceFactory>()
            bind<UnprovisionedDigitalOutFactory>().to<UnprovisionedDeviceResourceFactory>()
            bind<UnprovisionedEncoderFactory>().to<UnprovisionedDeviceResourceFactory>()
            bind<UnprovisionedPiezoelectricSpeakerFactory>().to<UnprovisionedDeviceResourceFactory>()
            bind<UnprovisionedSerialConnectionFactory>().to<UnprovisionedDeviceResourceFactory>()
            bind<UnprovisionedServoFactory>().to<UnprovisionedDeviceResourceFactory>()
            bind<UnprovisionedStepperFactory>().to<UnprovisionedDeviceResourceFactory>()
            bind<UnprovisionedUltrasonicFactory>().to<UnprovisionedDeviceResourceFactory>()
        }
    }
}
