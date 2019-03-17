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
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId

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
            4, 14, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 32, 33
        ).map { it.toByte() }

        private val analogInPins = listOf(
            4, 14, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 32, 33
        ).map { it.toByte() }

        private val analogOutPWMPins = listOf(
            4, 5, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 32, 33
        ).map { it.toByte() }

        private val analogOutDACPins = listOf(25, 26).map { it.toByte() }

        override fun isResourceInRange(resourceId: ResourceId): Boolean {
            val pins = when (resourceId.attachmentPoint) {
                is DefaultAttachmentPoints.Pin -> listOf(resourceId.attachmentPoint.pinNumber)

                is DefaultAttachmentPoints.PinGroup ->
                    resourceId.attachmentPoint.pinNumbers.toList()

                else -> return false
            }

            return pins.fold(true) { acc, elem ->
                acc && (elem in digitalInPins || elem in digitalOutPins || elem in analogInPins ||
                    elem in analogOutPWMPins || elem in analogOutDACPins)
            }
        }
    }

    object UnknownDevice : DefaultDeviceTypes("Unknown Device") {
        override fun isResourceInRange(resourceId: ResourceId) = true
    }
}
