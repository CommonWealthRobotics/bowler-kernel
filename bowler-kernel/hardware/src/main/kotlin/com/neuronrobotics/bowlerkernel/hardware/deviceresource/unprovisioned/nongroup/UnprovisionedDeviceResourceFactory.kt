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
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup

import arrow.core.Either
import arrow.core.left
import arrow.effects.IO
import arrow.effects.extensions.io.semigroup.maybeCombine
import arrow.typeclasses.Semigroup
import com.google.common.base.Throwables
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.group.AnalogInGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.group.DigitalInGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.group.DigitalOutGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.group.ProvisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.group.ServoGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.GenericResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.ProvisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.Servo
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.AttachmentPoint
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceType
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedAnalogInGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedAnalogInGroupFactory
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDigitalInGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDigitalInGroupFactory
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDigitalOutGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDigitalOutGroupFactory
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedServoGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedServoGroupFactory
import com.neuronrobotics.bowlerkernel.hardware.registry.HardwareRegistry
import com.neuronrobotics.bowlerkernel.hardware.registry.error.RegisterDeviceResourceError
import com.neuronrobotics.bowlerkernel.hardware.registry.error.RegisterDeviceResourceGroupError
import com.neuronrobotics.bowlerkernel.hardware.registry.error.RegisterError
import com.neuronrobotics.bowlerkernel.util.ServoLimits
import org.octogonapus.ktguava.collections.toImmutableList

/**
 * A facade for making any type of device resource. Requires the device to be connected or else
 * require creation will fail due to RPC timeout.
 */
@SuppressWarnings("TooManyFunctions")
class UnprovisionedDeviceResourceFactory(
    private val registry: HardwareRegistry
) : UnprovisionedAnalogInFactory,
    UnprovisionedAnalogOutFactory,
    UnprovisionedButtonFactory,
    UnprovisionedDigitalInFactory,
    UnprovisionedDigitalOutFactory,
    UnprovisionedEncoderFactory,
    UnprovisionedGenericResourceFactory,
    UnprovisionedPiezoelectricSpeakerFactory,
    UnprovisionedSerialConnectionFactory,
    UnprovisionedServoFactory,
    UnprovisionedStepperFactory,
    UnprovisionedUltrasonicFactory,
    UnprovisionedAnalogInGroupFactory,
    UnprovisionedDigitalInGroupFactory,
    UnprovisionedDigitalOutGroupFactory,
    UnprovisionedServoGroupFactory {

    // TODO: Make all the factories return IO instead of eagerly computing here
    private fun <T : UnprovisionedDeviceResource<R>, R : ProvisionedDeviceResource> makeUnprovisionedResource(
        device: BowlerDevice,
        resourceId: ResourceId,
        makeResource: (BowlerDevice, ResourceId) -> T
    ): Either<RegisterError, T> =
        device.isResourceInRange(resourceId).attempt().unsafeRunSync().fold(
            {
                RegisterDeviceResourceError.GenericError(
                    resourceId,
                    Throwables.getStackTraceAsString(it)
                ).left()
            },
            {
                if (it) {
                    registry.registerDeviceResource(device, resourceId, makeResource)
                } else {
                    RegisterDeviceResourceError.ResourceOutsideValidRangeError(resourceId).left()
                }
            }
        )

    @SuppressWarnings("MaxLineLength")
    private fun <T : UnprovisionedDeviceResourceGroup<R>, R : ProvisionedDeviceResourceGroup> makeUnprovisionedResourceGroup(
        device: BowlerDevice,
        resourceIds: ImmutableList<ResourceId>,
        makeResourceGroup: (BowlerDevice, ImmutableList<ResourceId>) -> T
    ): Either<RegisterError, T> =
        resourceIds.map { device.isResourceInRange(it) }.fold(IO.just(true)) { acc, elem ->
            acc.maybeCombine(
                object : Semigroup<Boolean> {
                    override fun Boolean.combine(b: Boolean) = this && b
                },
                elem
            )
        }.attempt().unsafeRunSync().fold(
            {
                RegisterDeviceResourceGroupError.GenericError(
                    resourceIds,
                    Throwables.getStackTraceAsString(it)
                ).left()
            },
            {
                if (it) {
                    registry.registerDeviceResourceGroup(device, resourceIds, makeResourceGroup)
                } else {
                    RegisterDeviceResourceGroupError.ResourceGroupMemberOutsideValidRangeError(
                        resourceIds
                    ).left()
                }
            }
        )

    override fun makeUnprovisionedAnalogIn(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedAnalogIn> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.AnalogIn, attachmentPoint)
        ) { registeredDevice, resourceId ->
            UnprovisionedAnalogIn(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedAnalogOut(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedAnalogOut> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.AnalogOut, attachmentPoint)
        ) { registeredDevice, resourceId ->
            UnprovisionedAnalogOut(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedButton(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedButton> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.Button, attachmentPoint)
        ) { registeredDevice, resourceId ->
            UnprovisionedButton(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedDigitalIn(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedDigitalIn> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.DigitalIn, attachmentPoint)
        ) { registeredDevice, resourceId ->
            UnprovisionedDigitalIn(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedDigitalOut(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedDigitalOut> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.DigitalOut, attachmentPoint)
        ) { registeredDevice, resourceId ->
            UnprovisionedDigitalOut(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedEncoder(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedEncoder> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.Encoder, attachmentPoint)
        ) { registeredDevice, resourceId ->
            UnprovisionedEncoder(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedGenericResource(
        device: BowlerDevice,
        resourceType: ResourceType,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedDeviceResource<GenericResource>> =
        makeUnprovisionedResource(
            device,
            ResourceId(resourceType, attachmentPoint)
        ) { registeredDevice, resourceId ->
            UnprovisionedGenericResource(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedPiezoelectricSpeaker(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedPiezoelectricSpeaker> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.PiezoelectricSpeaker, attachmentPoint)
        ) { registeredDevice, resourceId ->
            UnprovisionedPiezoelectricSpeaker(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedSerialConnection(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedSerialConnection> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.SerialConnection, attachmentPoint)
        ) { registeredDevice, resourceId ->
            UnprovisionedSerialConnection(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedServo(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint,
        limits: ServoLimits
    ): Either<RegisterError, UnprovisionedDeviceResource<Servo>> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.Servo, attachmentPoint)
        ) { registeredDevice, resourceId ->
            UnprovisionedServo(registeredDevice, resourceId, limits)
        }

    override fun makeUnprovisionedStepper(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedStepper> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.Stepper, attachmentPoint)
        ) { registeredDevice, resourceId ->
            UnprovisionedStepper(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedUltrasonic(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedUltrasonic> =
        makeUnprovisionedResource(
            device,
            ResourceId(DefaultResourceTypes.Ultrasonic, attachmentPoint)
        ) { registeredDevice, resourceId ->
            UnprovisionedUltrasonic(registeredDevice, resourceId)
        }

    override fun makeUnprovisionedAnalogInGroup(
        device: BowlerDevice,
        attachmentPoints: ImmutableList<AttachmentPoint>
    ): Either<RegisterError, UnprovisionedDeviceResourceGroup<AnalogInGroup>> =
        makeUnprovisionedResourceGroup(
            device,
            attachmentPoints.map {
                ResourceId(DefaultResourceTypes.AnalogIn, it)
            }.toImmutableList()
        ) { registeredDevice, resourceIds ->
            UnprovisionedAnalogInGroup(registeredDevice, resourceIds)
        }

    override fun makeUnprovisionedDigitalInGroup(
        device: BowlerDevice,
        attachmentPoints: ImmutableList<AttachmentPoint>
    ): Either<RegisterError, UnprovisionedDeviceResourceGroup<DigitalInGroup>> =
        makeUnprovisionedResourceGroup(
            device,
            attachmentPoints.map {
                ResourceId(DefaultResourceTypes.DigitalIn, it)
            }.toImmutableList()
        ) { registeredDevice, resourceIds ->
            UnprovisionedDigitalInGroup(registeredDevice, resourceIds)
        }

    override fun makeUnprovisionedDigitalOutGroup(
        device: BowlerDevice,
        attachmentPoints: ImmutableList<AttachmentPoint>
    ): Either<RegisterError, UnprovisionedDeviceResourceGroup<DigitalOutGroup>> =
        makeUnprovisionedResourceGroup(
            device,
            attachmentPoints.map {
                ResourceId(DefaultResourceTypes.DigitalOut, it)
            }.toImmutableList()
        ) { registeredDevice, resourceIds ->
            UnprovisionedDigitalOutGroup(registeredDevice, resourceIds)
        }

    override fun makeUnprovisionedServoGroup(
        device: BowlerDevice,
        attachmentPointsAndLimits: ImmutableList<Pair<AttachmentPoint, ServoLimits>>
    ): Either<RegisterError, UnprovisionedDeviceResourceGroup<ServoGroup>> =
        makeUnprovisionedResourceGroup(
            device,
            attachmentPointsAndLimits.map {
                ResourceId(DefaultResourceTypes.Servo, it.first)
            }.toImmutableList()
        ) { registeredDevice, resourceIds ->
            UnprovisionedServoGroup(
                registeredDevice,
                resourceIds,
                attachmentPointsAndLimits.map { it.second }.toImmutableList()
            )
        }
}
