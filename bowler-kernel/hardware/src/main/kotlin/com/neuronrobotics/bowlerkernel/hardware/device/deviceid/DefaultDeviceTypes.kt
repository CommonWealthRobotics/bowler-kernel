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
package com.neuronrobotics.bowlerkernel.hardware.device.deviceid

import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.util.isAllUnique

/**
 * The device types Bowler supports out-of-the-box.
 */
sealed class DefaultDeviceTypes(
    override val name: String
) : DeviceType {

    object Esp32wroom32 : DefaultDeviceTypes("ESP32-WROOM-32") {

        private val digitalInPins = listOf(
            4, 14, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 32, 33, 34, 35, 36, 39
        ).map { it.toByte() }

        private val digitalOutPins = listOf(
            2, 4, 5, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 32, 33
        ).map { it.toByte() }

        private val analogInPins = listOf(
            4, 14, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 32, 33, 34, 35, 36, 39
        ).map { it.toByte() }

        private val analogOutPins = listOf(
            4, 5, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 32, 33
        ).map { it.toByte() }

        private val serialPins = listOf(1, 3).map { it.toByte() }.toByteArray()

        override fun isResourceInRange(resourceId: ResourceId): Boolean {
            return when (val p = resourceId.attachmentPoint) {
                is DefaultAttachmentPoints.Pin -> checkPin(p)
                is DefaultAttachmentPoints.PinGroup -> checkPinGroup(resourceId, p)
                else -> false
            }
        }

        private fun checkPin(p: DefaultAttachmentPoints.Pin) =
            p.pinNumber in digitalInPins ||
                p.pinNumber in digitalOutPins ||
                p.pinNumber in analogInPins ||
                p.pinNumber in analogOutPins

        private fun checkPinGroup(
            resourceId: ResourceId,
            p: DefaultAttachmentPoints.PinGroup
        ) = when (resourceId.resourceType) {
            is DefaultResourceTypes.Encoder -> checkEncoder(p)
            is DefaultResourceTypes.SerialConnection -> checkSerial(p)
            is DefaultResourceTypes.Ultrasonic -> checkUltrasonic(p)
            is DefaultResourceTypes.Stepper -> checkStepper(p)
            else -> false
        }

        private fun checkStepper(p: DefaultAttachmentPoints.PinGroup) =
            p.pinNumbers.isAllUnique() &&
                p.pinNumbers.size in listOf(2, 4, 5) &&
                p.pinNumbers.all { it in digitalOutPins }

        private fun checkEncoder(p: DefaultAttachmentPoints.PinGroup) =
            p.pinNumbers.isAllUnique() && p.pinNumbers.all { it in digitalInPins }

        private fun checkSerial(p: DefaultAttachmentPoints.PinGroup) =
            p.pinNumbers.isAllUnique() && p.pinNumbers.all { it in serialPins }

        private fun checkUltrasonic(p: DefaultAttachmentPoints.PinGroup) =
            p.pinNumbers.isAllUnique() && when (p.pinNumbers.size) {
                2 -> (p.pinNumbers[0] in digitalInPins && p.pinNumbers[1] in digitalOutPins) ||
                    (p.pinNumbers[1] in digitalInPins && p.pinNumbers[0] in digitalOutPins)

                else -> false
            }
    }

    object UnknownDevice : DefaultDeviceTypes("Unknown Device") {
        override fun isResourceInRange(resourceId: ResourceId) = true
    }
}
