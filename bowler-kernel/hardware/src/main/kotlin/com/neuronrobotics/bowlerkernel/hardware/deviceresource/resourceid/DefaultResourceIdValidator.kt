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

import arrow.core.Option

class DefaultResourceIdValidator : ResourceIdValidator {

    override fun validateIsReadType(resourceId: ResourceId): Option<String> {
        return if (resourceId.resourceType is DefaultResourceTypes) {
            when (resourceId.resourceType) {
                is DefaultResourceTypes.AnalogIn,
                is DefaultResourceTypes.DigitalIn,
                is DefaultResourceTypes.SerialConnection,
                is DefaultResourceTypes.Servo,
                is DefaultResourceTypes.Encoder,
                is DefaultResourceTypes.Button,
                is DefaultResourceTypes.Ultrasonic -> Option.empty()

                else -> Option.just(
                    """
                    |This resource is not a read type:
                    |$this
                    """.trimMargin()
                )
            }
        } else {
            // Can't validate what we don't own
            Option.just(
                """
                |Unknown resource:
                |$this
                """.trimMargin()
            )
        }
    }

    override fun validateIsWriteType(resourceId: ResourceId): Option<String> {
        return if (resourceId.resourceType is DefaultResourceTypes) {
            when (resourceId.resourceType) {
                is DefaultResourceTypes.AnalogOut,
                is DefaultResourceTypes.DigitalOut,
                is DefaultResourceTypes.SerialConnection,
                is DefaultResourceTypes.Servo,
                is DefaultResourceTypes.Stepper,
                is DefaultResourceTypes.PiezoelectricSpeaker -> Option.empty()

                else -> Option.just(
                    """
                    |This resource is not a write type:
                    |$this
                    """.trimMargin()
                )
            }
        } else {
            // Can't validate what we don't own
            Option.just(
                """
                |Unknown resource:
                |$this
                """.trimMargin()
            )
        }
    }
}
