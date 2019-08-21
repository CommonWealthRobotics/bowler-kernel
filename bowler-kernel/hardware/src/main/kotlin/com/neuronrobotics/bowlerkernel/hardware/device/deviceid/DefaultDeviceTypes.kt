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

import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId

/**
 * The device types Bowler supports out-of-the-box.
 */
sealed class DefaultDeviceTypes(
    override val name: String,
    val firstPinNumber: Int,
    val numberOfPins: Int
) : DeviceType {

    object Esp32wroom32 : DefaultDeviceTypes("ESP32-WROOM-32", 0, 40) {

        private val implementation = implementDeviceType(Esp32wroom32) {
            digitalOut(2, 4..5, 12..19, 21..23, 25..27, 32..33)
            digitalIn(4, 14, 16..19, 21..23, 25..27, 32..36, 39)
            analogOut(4, 5, 12..19, 21..23, 25..27, 32..33)
            analogIn(4, 14, 16..19, 21..23, 25..27, 32..36, 39)
            serial(1 to 3)
        }

        override fun isResourceInRange(resourceId: ResourceId) =
            implementation.isResourceInRange(resourceId)
    }

    object Teensy35 : DefaultDeviceTypes("Teensy 3.5", 0, 58) {

        private val implementation = implementDeviceType(Teensy35) {
            digitalOut(0..57)
            digitalIn(0..57)
            analogOut(2..10, 14, 20..23, 29..30, 35..38)
            analogIn(0..26)
            serial(1 to 0, 10 to 9, 8 to 7, 32 to 31, 33 to 34, 48 to 47)
        }

        override fun isResourceInRange(resourceId: ResourceId) =
            implementation.isResourceInRange(resourceId)
    }

    object UnknownDevice : DefaultDeviceTypes("Unknown Device", 0, 0) {
        override fun isResourceInRange(resourceId: ResourceId) = true
    }
}
