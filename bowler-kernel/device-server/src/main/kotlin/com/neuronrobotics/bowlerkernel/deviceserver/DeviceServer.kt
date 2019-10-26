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

/**
 * A server that communicates with the device using reliable and unreliable transport.
 */
interface DeviceServer {

    /**
     * Open a connection with the device.
     */
    fun connect(): IO<Unit>

    /**
     * Close the current connection with the device.
     */
    fun disconnect(): IO<Unit>

    /**
     * Add a packet that uses reliable transport.
     *
     * @param id The packet id.
     */
    fun addReliable(id: Byte)

    /**
     * Add a packet that uses unreliable transport.
     *
     * @param id The packet id.
     * @param maxRetries The maximum number of retries if the device never replied.
     */
    fun addUnreliable(id: Byte, maxRetries: Int)

    /**
     * Write data to the packet using its transport mode.
     *
     * @param id The packet id.
     * @param payload The data to send to the packet.
     * @return The data from the response.
     */
    fun write(id: Byte, payload: ByteArray): IO<ByteArray>

    companion object {

        const val SERVER_MANAGEMENT_PACKET_ID: Byte = 1

        const val OPERATION_DISCONNECT_ID: Byte = 1
        const val OPERATION_ADD_ENSURED_PACKETS: Byte = 2

        const val STATUS_ACCEPTED: Byte = 1
        const val STATUS_REJECTED_GENERIC: Byte = 2
    }
}
