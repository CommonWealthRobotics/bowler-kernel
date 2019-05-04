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

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class DefaultResourceIdValidatorTest {

    private val validator = DefaultResourceIdValidator()

    @ParameterizedTest
    @MethodSource("resourceValidationSource")
    fun `test resource types are validated correctly`(
        data: Pair<ResourceId, List<ResourceType>>
    ) {
        if (data.second.isEmpty()) {
            fail { "No resource types given" }
        }

        if (data.second.contains(ResourceType.Write)) {
            assertTrue(validator.validateIsWriteType(data.first.resourceType).isRight())
        }

        if (data.second.contains(ResourceType.Read)) {
            assertTrue(validator.validateIsReadType(data.first.resourceType).isRight())
        }
    }

    companion object {

        enum class ResourceType {
            Write, Read
        }

        @Suppress("unused")
        @JvmStatic
        fun resourceValidationSource() = listOf(
            ResourceId(
                DefaultResourceTypes.DigitalIn,
                DefaultAttachmentPoints.Pin(1)
            ) to listOf(ResourceType.Read),
            ResourceId(
                DefaultResourceTypes.DigitalOut,
                DefaultAttachmentPoints.Pin(1)
            ) to listOf(ResourceType.Write),
            ResourceId(
                DefaultResourceTypes.AnalogIn,
                DefaultAttachmentPoints.Pin(1)
            ) to listOf(ResourceType.Read),
            ResourceId(
                DefaultResourceTypes.AnalogOut,
                DefaultAttachmentPoints.Pin(1)
            ) to listOf(ResourceType.Write),
            ResourceId(
                DefaultResourceTypes.SerialConnection,
                DefaultAttachmentPoints.USBPort(1)
            ) to listOf(ResourceType.Read, ResourceType.Write),
            ResourceId(
                DefaultResourceTypes.Servo,
                DefaultAttachmentPoints.Pin(1)
            ) to listOf(ResourceType.Write),
            ResourceId(
                DefaultResourceTypes.Stepper,
                DefaultAttachmentPoints.PinGroup(byteArrayOf(1, 2, 3, 4))
            ) to listOf(ResourceType.Write),
            ResourceId(
                DefaultResourceTypes.Encoder,
                DefaultAttachmentPoints.PinGroup(byteArrayOf(1, 2))
            ) to listOf(ResourceType.Read),
            ResourceId(
                DefaultResourceTypes.Button,
                DefaultAttachmentPoints.Pin(1)
            ) to listOf(ResourceType.Read),
            ResourceId(
                DefaultResourceTypes.Ultrasonic,
                DefaultAttachmentPoints.PinGroup(byteArrayOf(1, 2))
            ) to listOf(ResourceType.Read),
            ResourceId(
                DefaultResourceTypes.PiezoelectricSpeaker,
                DefaultAttachmentPoints.Pin(1)
            ) to listOf(ResourceType.Write)
        )
    }
}
