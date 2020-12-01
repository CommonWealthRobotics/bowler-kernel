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
package com.commonwealthrobotics.bowlerkernel.kerneldiscovery

import arrow.core.Tuple2
import mu.KotlinLogging
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

object NameClient {

    /**
     * Scans for name servers. Used by clients to discover kernels on the network.
     *
     * @param multicastGroup The multicast group address to send packets to.
     * @param port The destination port to send UDP packets to.
     * @param timeoutMs The timeout in milliseconds used when waiting for responses from a candidate name server.
     * If there are no replies within this time period, then the scanning operation finishes.
     * @return All the discovered names and their corresponding addresses.
     */
    fun scan(
        multicastGroup: InetAddress = NameServer.defaultMulticastGroup,
        port: Int = NameServer.defaultPort,
        timeoutMs: Int = 1000
    ): List<Tuple2<String, InetAddress>> {
        val socket = DatagramSocket()
        socket.soTimeout = timeoutMs

        logger.debug { "Sending to $multicastGroup:$port" }
        socket.send(DatagramPacket(NameServer.getNameBytes, NameServer.getNameBytes.size, multicastGroup, port))

        val names = mutableListOf<Tuple2<String, InetAddress>>()
        while (true) {
            try {
                val reply = DatagramPacket(ByteArray(NameServer.maxReplyLength), NameServer.maxReplyLength)
                socket.receive(reply)
                logger.debug { "From ${reply.address}:${reply.port}: ${reply.data.joinToString()}" }

                val numBytes = reply.data[0].toInt()
                val name = reply.data.toList().subList(1, numBytes + 1).toByteArray().decodeToString()
                names.add(Tuple2(name, reply.address))
            } catch (ex: SocketTimeoutException) {
                break
            }
        }

        socket.close()
        return names
    }

    private val logger = KotlinLogging.logger { }
}
