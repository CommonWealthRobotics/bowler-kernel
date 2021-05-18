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

import mu.KotlinLogging
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer

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
    ): List<Pair<String, InetAddress>> {
        val socket = DatagramSocket()
        socket.soTimeout = timeoutMs

        logger.debug { "Sending to $multicastGroup:$port" }
        socket.send(DatagramPacket(NameServer.getNameBytes, NameServer.getNameBytes.size, multicastGroup, port))

        val names = mutableListOf<Pair<String, InetAddress>>()
        while (true) {
            try {
                val reply = DatagramPacket(ByteArray(NameServer.maxReplyLength), NameServer.maxReplyLength)
                socket.receive(reply)
                logger.debug { "From ${reply.address}:${reply.port}: ${reply.data.joinToString()}" }

                val numBytes = reply.data[0].toInt()
                val name = reply.data.toList().subList(1, numBytes + 1).toByteArray().decodeToString()
                names.add(Pair(name, reply.address))
            } catch (ex: SocketTimeoutException) {
                break
            }
        }

        socket.close()
        return names
    }

    /**
     * Requests the kernel server's port number from the name server at the [address] and [port].
     *
     * @param address The address to send packets to.
     * @param port The destination port to send UDP packets to.
     * @param timeoutMs The timeout in milliseconds used when waiting for a response.
     * @return The kernel server's port number. Could be `-1` if it is not running.
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    fun getGrpcPort(
        address: InetAddress,
        port: Int = NameServer.defaultPort,
        timeoutMs: Int = 1000,
        attempts: Int = 10,
    ): Int {
        val socket = DatagramSocket()
        socket.soTimeout = timeoutMs

        logger.debug { "Sending to $address:$port" }
        socket.send(
            DatagramPacket(NameServer.getKernelServerPortBytes, NameServer.getKernelServerPortBytes.size, address, port)
        )

        val reply = DatagramPacket(ByteArray(NameServer.maxReplyLength), NameServer.maxReplyLength)
        for (i in 1..attempts) {
            try {
                socket.receive(reply)
                logger.debug { "From ${reply.address}:${reply.port}: ${reply.data.joinToString()}" }

                val numBytes = reply.data[0]
                check(numBytes == Int.SIZE_BYTES.toByte())
                val grpcPort = ByteBuffer.allocate(Int.SIZE_BYTES).put(reply.data, 1, Int.SIZE_BYTES).rewind().int

                socket.close()
                return grpcPort
            } catch (ex: SocketTimeoutException) {
                // Ignored
            }
        }

        throw IllegalStateException(
            "Failed to get the kernel server's port number because all $attempts attempts timed out."
        )
    }

    private val logger = KotlinLogging.logger { }
}
