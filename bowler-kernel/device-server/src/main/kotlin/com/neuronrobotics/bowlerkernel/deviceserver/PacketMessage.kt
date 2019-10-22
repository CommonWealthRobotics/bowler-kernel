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
package com.neuronrobotics.bowlerkernel.deviceserver

/**
 * A message sent between PC and device.
 *
 * @param packetId The ID of the packet (matches which packet event handler on the device the packet
 * is sent to).
 * @param payload The payload of the packet (not including header information; this is just the
 * packet's data).
 */
data class PacketMessage(
    val packetId: Byte,
    val payload: ByteArray
) {

    init {
        require(payload.size == 61) {
            "The payload must have size 61 (was ${payload.size})."
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PacketMessage) return false

        if (packetId != other.packetId) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packetId.toInt()
        result = 31 * result + payload.contentHashCode()
        return result
    }

    companion object {

        fun fromBytes(data: ByteArray) = PacketMessage(
            data.id,
            data.sliceArray(3 until data.size)
        )
    }
}
