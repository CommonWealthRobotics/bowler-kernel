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

import arrow.effects.IO

interface DeviceServer {

    fun connect(): IO<Unit>

    fun disconnect(): IO<Unit>

    // uses reliable transport
    fun addReliable(id: Byte)

    // uses unreliable transport
    fun addUnreliable(id: Byte, maxRetries: Int)

    // write to the packet and wait for the reply. uses its transport mode
    fun write(id: Byte, payload: ByteArray): IO<ByteArray>
}
