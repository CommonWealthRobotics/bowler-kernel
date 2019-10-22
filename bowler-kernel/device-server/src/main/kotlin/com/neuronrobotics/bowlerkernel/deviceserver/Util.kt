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
 * Creates a payload using the given [bytes] with padding up to the size of the payload.
 *
 * @param bytes The bytes at the start of the payload
 * @param padding Whether to pad the payload up to the correct size.
 * @return The payload.
 */
fun getPayload(vararg bytes: Byte, padding: Boolean = true): ByteArray =
    if (padding)
        bytes + (1..61 - bytes.size).map { 0.toByte() }
    else
        bytes

internal var ByteArray.id
    get() = this[0]
    set(value) {
        this[0] = value
    }

internal var ByteArray.seq
    get() = this[1]
    set(value) {
        this[1] = value
    }

internal var ByteArray.ack
    get() = this[2]
    set(value) {
        this[2] = value
    }
