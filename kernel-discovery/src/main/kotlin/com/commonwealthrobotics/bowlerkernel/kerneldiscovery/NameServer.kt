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
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.streams.asSequence

/**
 * A name server used by clients to discover kernels on the network.
 *
 * @param desiredName The name this kernel will respond with. If this name is not unique between all kernels on this
 * network, a suffix will be appended to make it unique.
 * @param multicastGroup The multicast group address this server will join. This should be in the IANA Scoped
 * Multicast Range for Organization-Local Scope (239.0.0.0-239.255.255.255) specified in RFC5771.
 * @param desiredPort The port the server should bind to. This should be in the Dynamic Port Number Range
 * (49152-65535) specified in RFC6335.
 */
class NameServer(
    private val desiredName: String,
    private val multicastGroup: InetAddress = defaultMulticastGroup,
    private val desiredPort: Int = defaultPort
) {

    val name: String

    init {
        // Check the name will fit in the payload, even if we need to make it unique (1 byte header, 24 byte suffix)
        require(desiredName.length + 24 < maxReplyLength + 1)
        name = determineUniqueName(desiredName, NameClient.scan(multicastGroup, desiredPort).map { it.a })

        // Check the unique name will fit in the payload
        check(name.length < maxReplyLength + 1)
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
            val payload = ByteArray(nameBytes.size + 1) {
                if (it == 0) {
                    nameBytes.size.toByte()
                } else {
                    nameBytes[it - 1]
                }
            }

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
                    // Ignored
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

        /**
         * The default multicast group the kernel server joins. This is within the IANA Scoped Multicast Range for
         * Organization-Local Scope (239.0.0.0-239.255.255.255) specified in RFC5771.
         */
        val defaultMulticastGroup: InetAddress = InetAddress.getByAddress(
            byteArrayOf(239.toByte(), 255.toByte(), 255.toByte(), 255.toByte())
        )

        /**
         * The default port the kernel server listens on. This is within the Dynamic Port Number Range (49152-65535)
         * specified in RFC6335.
         */
        const val defaultPort = 62657

        /**
         * The maximum payload size of a reply.
         */
        const val maxReplyLength = 100

        /**
         * Returns a name that is unique between all the names in [allNames]
         *
         * @param name The desired name.
         * @param allNames The names on the network.
         * @return A unique name.
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        internal fun determineUniqueName(name: String, allNames: List<String>): String = if (name in allNames) {
            // The name is not unique so we need to add something to make it unique. Try a MAC address first. Then
            // try random numbers.
            val newName = NetworkInterface.networkInterfaces().asSequence()
                .filter {
                    !it.isVirtual &&
                        it.isUp &&
                        !it.name.startsWith("vir") &&
                        !it.name.startsWith("lo") &&
                        it.hardwareAddress != null
                }
                .map { name + "-" + it.hardwareAddress.asUByteArray().joinToString(":") }
                .firstOrNull { it !in allNames }
                ?: sequence<String> { name + "-" + Random.nextInt() }.first { it !in allNames }
            logger.debug { "The name $name was not unique on this network. Renaming to $newName" }
            newName
        } else {
            // The name was unique, we are done
            name
        }
    }
}
