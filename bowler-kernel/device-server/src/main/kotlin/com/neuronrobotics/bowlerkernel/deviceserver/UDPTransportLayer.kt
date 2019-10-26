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

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Uses the UDP layer.
 *
 * @param address The device IP.
 * @param port The port the device is listening on.
 * @param timeout The read timeout in milliseconds.
 */
class UDPTransportLayer(
    private val address: InetAddress,
    private val port: Int,
    private val timeout: Int = 50,
    private val packetLength: Int = 64
) : TransportLayer {

    private val sock = DatagramSocket().apply { soTimeout = timeout }

    override fun connect() {
        // UDP has no connection state
    }

    override fun disconnect() {
        // UDP has no connection state
    }

    override fun readBytes(): ByteArray {
        val receiveData = ByteArray(packetLength)
        val receivePacket = DatagramPacket(receiveData, packetLength)
        sock.receive(receivePacket)
        return receivePacket.data
    }

    override fun writeBytes(data: ByteArray) {
        val packet = DatagramPacket(data, packetLength, address, port)
        sock.send(packet)
    }
}
