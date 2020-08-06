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
package com.commonwealthrobotics.bowlerkernel.deviceserver

/**
 * The layer used to transport packets between PC and device.
 */
interface TransportLayer {

    /**
     * Start a new connection.
     */
    fun connect()

    /**
     * Close the current connection, or do nothing if there is no connection.
     */
    fun disconnect()

    /**
     * Reads a packet from the device.
     *
     * @return The data.
     */
    fun readBytes(): ByteArray

    /**
     * Writes a packet to the device.
     *
     * @param data The data to send.
     */
    fun writeBytes(data: ByteArray)
}
