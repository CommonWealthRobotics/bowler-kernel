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

/**
 * The attachment points Bowler supports out-of-the-box. Uses a continuous range of bytes from
 * [getLowestTypeNumber] through [getHighestTypeNumber]. Any numbers outside that range are
 * available for adding custom attachment points.
 */
sealed class DefaultAttachmentPoints(
    override val type: Byte,
    override val data: ByteArray = byteArrayOf()
) : AttachmentPoint {

    /**
     * A single pin. The data is the pin number which is converted to an unsigned byte.
     *
     * @param pinNumber The pin number (converted to an unsigned byte).
     */
    data class Pin(val pinNumber: Int) : DefaultAttachmentPoints(
        1,
        byteArrayOf(pinNumber.toByte())
    )

    /**
     * A group of pins. The data is the number of pins followed by the pin numbers.
     *
     * @param pinNumbers The pin numbers (converted to unsigned bytes).
     */
    data class PinGroup(val pinNumbers: ByteArray) :
        DefaultAttachmentPoints(
            2,
            byteArrayOf(pinNumbers.size.toByte()) + pinNumbers
        ) {

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
     * @param portNumber The device-specific port number (converted to an unsigned byte).
     */
    data class USBPort(val portNumber: Int) :
        DefaultAttachmentPoints(3, byteArrayOf(portNumber.toByte()))

    /**
     * The lowest used type number.
     */
    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getLowestTypeNumber(): Byte = 1

    /**
     * The highest used type number.
     */
    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getHighestTypeNumber(): Byte = 3
}
