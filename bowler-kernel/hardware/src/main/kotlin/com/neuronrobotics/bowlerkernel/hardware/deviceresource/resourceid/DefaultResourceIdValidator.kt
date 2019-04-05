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
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid

import arrow.core.Either
import arrow.core.left
import arrow.core.right

class DefaultResourceIdValidator : ResourceIdValidator {

    override fun valdiateAttachmentPoint(attachmentPoint: AttachmentPoint): Either<String, Unit> {
        TODO("not implemented")
    }

    override fun validateIsReadType(resourceType: ResourceType): Either<String, Unit> {
        return if (resourceType is DefaultResourceTypes) {
            when (resourceType) {
                is DefaultResourceTypes.AnalogIn,
                is DefaultResourceTypes.DigitalIn,
                is DefaultResourceTypes.SerialConnection,
                is DefaultResourceTypes.Servo,
                is DefaultResourceTypes.Encoder,
                is DefaultResourceTypes.Button,
                is DefaultResourceTypes.Ultrasonic -> Unit.right()

                else ->
                    """
                    |This resource is not a read type:
                    |$this
                    """.trimMargin().left()
            }
        } else {
            // Can't validate what we don't own
            """
            |Unknown resource:
            |$this
            """.trimMargin().left()
        }
    }

    override fun validateIsWriteType(resourceType: ResourceType): Either<String, Unit> {
        return if (resourceType is DefaultResourceTypes) {
            when (resourceType) {
                is DefaultResourceTypes.AnalogOut,
                is DefaultResourceTypes.DigitalOut,
                is DefaultResourceTypes.SerialConnection,
                is DefaultResourceTypes.Servo,
                is DefaultResourceTypes.Stepper,
                is DefaultResourceTypes.PiezoelectricSpeaker -> Unit.right()

                else ->
                    """
                    |This resource is not a write type:
                    |$this
                    """.trimMargin().left()
            }
        } else {
            // Can't validate what we don't own
            """
            |Unknown resource:
            |$this
            """.trimMargin().left()
        }
    }
}
