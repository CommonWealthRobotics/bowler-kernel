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
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * A name server used by clients to discover kernels on the network.
 *
 * @param name The name this kernel will respond with.
 * @param multicastGroup The multicast group address this server will join.
 * @param desiredPort The port the server should bind to.
 */
class NameServer(
    private val name: String,
    private val multicastGroup: InetAddress = defaultMulticastGroup,
    private val desiredPort: Int = defaultPort
) {

    init {
        // Check the name will fit in the payload (1 byte header)
        require(name.length < maxReplyLength + 1)
    }

    /**
     * The address the UDP socket has bound to.
     */
    var address: InetAddress? = null
        private set

    /**
     * The port the UDP socket has bound to.
     */
    var port: Int? = null
        private set

    /**
     * Whether the server is running.
     */
    var isRunning = AtomicBoolean(false)
        private set

    private var thread: Thread? = null

    /**
     * Starts the server if it is not running. Does nothing if it is not running.
     */
    fun ensureStarted() {
        synchronized(this) {
            if (thread == null) {
                logger.debug { "Starting name server." }
                thread = thread { run() }
            }
        }
    }

    /**
     * Stops the server is it is running. Does nothing if it is not running.
     */
    fun ensureStopped() {
        synchronized(this) {
            if (thread != null) {
                logger.debug { "Stopping name server." }
                thread!!.interrupt()
                logger.debug { "Waiting for name server to stop." }
                thread!!.join()
                thread = null
                isRunning.set(false)
            }
        }
    }

    private fun run() {
        val socket = MulticastSocket(desiredPort)
        socket.joinGroup(multicastGroup)
        socket.soTimeout = 100
        address = socket.localAddress
        port = socket.localPort

        logger.debug { "Name server running at $address:$port" }
        isRunning.set(true)

        try {
            val packetBuf = ByteArray(8)
            val packet = DatagramPacket(packetBuf, packetBuf.size)
            val nameBytes = name.encodeToByteArray()
            val payload = byteArrayOf(nameBytes.size.toByte(), *nameBytes)

            while (!Thread.interrupted()) {
                try {
                    socket.receive(packet)
                    if (packet.data.contentEquals(getNameBytes)) {
                        check(payload.size <= maxReplyLength) {
                            "Payload size invariant broke: payload.size=${payload.size} maxReplyLength=$maxReplyLength"
                        }
                        socket.send(DatagramPacket(payload, payload.size, packet.address, packet.port))
                    }
                } catch (ex: SocketTimeoutException) {
                }

                try {
                    Thread.sleep(10)
                } catch (ex: InterruptedException) {
                    logger.debug { "Stopping name server thread due to interruption." }
                    break
                }
            }
        } finally {
            socket.close()
            isRunning.set(false)
        }
    }

    companion object {

        private val logger = KotlinLogging.logger { }

        val getNameBytes = "get-name".encodeToByteArray()
        val defaultMulticastGroup: InetAddress = InetAddress.getByAddress(
            byteArrayOf(239.toByte(), 255.toByte(), 255.toByte(), 255.toByte())
        )
        const val defaultPort = 1776
        const val maxReplyLength = 10
    }
}
