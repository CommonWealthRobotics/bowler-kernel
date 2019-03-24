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
package com.neuronrobotics.bowlerkernel.hardware.protocol

/**
 * Creates a mock payload. The payload starts with the given [bytes] and is padded with
 * zeroes to a length of [SimplePacketComsProtocol.PAYLOAD_SIZE].
 *
 * @param bytes The first bytes in the payload.
 * @return The payload.
 */
internal fun getPayload(vararg bytes: Byte): ByteArray =
    bytes + (1..SimplePacketComsProtocol.PAYLOAD_SIZE - bytes.size).map { 0.toByte() }
