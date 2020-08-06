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
package com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid

import java.nio.ByteBuffer

/**
 * The attachment points Bowler supports out-of-the-box. Uses a continuous range of bytes from
 * [getLowestTypeNumber] through [getHighestTypeNumber]. Any numbers outside that range are
 * available for adding custom attachment points.
 */
sealed class AttachmentPoint(
    val type: Byte,
    val data: ByteArray = byteArrayOf()
) {

    /**
     * A single pin. The data is the pin number which is converted to an unsigned byte.
     *
     * @param pinNumber The pin number.
     */
    data class Pin(val pinNumber: Byte) :
        AttachmentPoint(1, byteArrayOf(pinNumber))

    /**
     * A group of pins. The data is the number of pins followed by the pin numbers.
     *
     * @param pinNumbers The pin numbers.
     */
    data class PinGroup(val pinNumbers: ByteArray) :
        AttachmentPoint(2, byteArrayOf(pinNumbers.size.toByte()) + pinNumbers) {

        init {
            require(pinNumbers.size < 58)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PinGroup

            if (!pinNumbers.contentEquals(other.pinNumbers)) return false

            return true
        }

        override fun hashCode(): Int = pinNumbers.contentHashCode()
    }

    /**
     * A USB port on the device. The data is the port number converted to an unsigned byte.
     *
     * @param portNumber The device-specific port number.
     */
    data class USBPort(val portNumber: Byte) :
        AttachmentPoint(3, byteArrayOf(portNumber))

    /**
     * A pin with specific duty cycle limits, meant to be used to set a servo's duty cycle limits.
     *
     * @param pinNumber The pin number.
     * @param minUsLow The minimum microseconds LOW.
     * @param maxUsHigh The maximum microseconds HIGH.
     * @param timerWidth The timer width in bits.
     */
    data class PwmPin(
        val pinNumber: Byte,
        val minUsLow: Short,
        val maxUsHigh: Short,
        val timerWidth: Byte
    ) :
        AttachmentPoint(
            4,
            byteArrayOf(pinNumber) + minUsLow.asByteArray() + maxUsHigh.asByteArray() + timerWidth
        )

    /**
     * No attachment point data.
     */
    object None : AttachmentPoint(5)

    /**
     * The lowest used type number.
     */
    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getLowestTypeNumber(): Byte = 1

    /**
     * The highest used type number.
     */
    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getHighestTypeNumber(): Byte = 5
}

private fun Short.asByteArray(): ByteArray =
    ByteBuffer.allocate(Short.SIZE_BYTES).putShort(this).array()
