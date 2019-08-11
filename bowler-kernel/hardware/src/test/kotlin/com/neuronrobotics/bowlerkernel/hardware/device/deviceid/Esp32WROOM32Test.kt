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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.TimeUnit

// TODO: Finish this
@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class Esp32WROOM32Test {

    @ParameterizedTest
    @MethodSource("digitalOutPinsSource")
    fun `test digital out pins`(pinNumber: Byte) {
        val actual = DefaultDeviceTypes.Esp32wroom32.isResourceInRange(
            ResourceId(
                DefaultResourceTypes.DigitalOut,
                DefaultAttachmentPoints.Pin(pinNumber)
            )
        )

        assertTrue(actual)
    }

    @ParameterizedTest
    @MethodSource("digitalInPinsSource")
    fun `test digital in pins`(pinNumber: Byte) {
        val actual = DefaultDeviceTypes.Esp32wroom32.isResourceInRange(
            ResourceId(
                DefaultResourceTypes.DigitalIn,
                DefaultAttachmentPoints.Pin(pinNumber)
            )
        )

        assertTrue(actual)
    }

    @ParameterizedTest
    @MethodSource("analogOutPinsSource")
    fun `test analog out pins`(pinNumber: Byte) {
        val actual = DefaultDeviceTypes.Esp32wroom32.isResourceInRange(
            ResourceId(
                DefaultResourceTypes.AnalogOut,
                DefaultAttachmentPoints.Pin(pinNumber)
            )
        )

        assertTrue(actual)
    }

    @ParameterizedTest
    @MethodSource("analogInPinsSource")
    fun `test analog in pins`(pinNumber: Byte) {
        val actual = DefaultDeviceTypes.Esp32wroom32.isResourceInRange(
            ResourceId(
                DefaultResourceTypes.AnalogIn,
                DefaultAttachmentPoints.Pin(pinNumber)
            )
        )

        assertTrue(actual)
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun digitalOutPinsSource() =
            listOf<Byte>(2, 4, 5, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 32, 33)

        @Suppress("unused")
        @JvmStatic
        fun digitalInPinsSource() =
            listOf<Byte>(4, 14, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 32, 33, 34, 35, 36, 39)

        @Suppress("unused")
        @JvmStatic
        fun analogOutPinsSource() =
            listOf<Byte>(4, 5, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 32, 33)

        @Suppress("unused")
        @JvmStatic
        fun analogInPinsSource() =
            listOf<Byte>(4, 14, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 32, 33, 34, 35, 36, 39)
    }
}
