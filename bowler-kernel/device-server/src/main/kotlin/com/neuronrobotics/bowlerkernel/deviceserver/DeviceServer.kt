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

import java.net.SocketTimeoutException
import java.util.ArrayDeque

class DeviceServer(
    private val transportLayer: TransportLayer
) {

    private val sendQueue = ArrayDeque<PacketMessage>()
    private val receiveQueue = ArrayDeque<PacketMessage>()
    private var state = States.waitFor0
    private lateinit var lastMessage: PacketMessage

    /**
     * Send the packet and wait for a response using reliable transport.
     *
     * @param packetMessage The packet to send.
     * @return The reply packet.
     */
    fun sendReceiveReliable(packetMessage: PacketMessage): PacketMessage {
        sendQueue.addLast(packetMessage)

        while (receiveQueue.isEmpty()) {
            loop()
        }

        return receiveQueue.pop()
    }

    /**
     * Send the packet and wait for a response using unreliable transport.
     *
     * @param packetMessage The packet to send.
     * @return The reply packet.
     */
    fun sendReceiveUnreliable(packetMessage: PacketMessage, maxRetries: Int = 10): PacketMessage {
        transportLayer.writeBytes(makePacketData(packetMessage, 0, 0))
        return try {
            PacketMessage.fromBytes(transportLayer.readBytes())
        } catch (ex: SocketTimeoutException) {
            check(maxRetries > 0)
            sendReceiveUnreliable(packetMessage, maxRetries - 1)
        }
    }

    private fun makePacketData(data: PacketMessage, seqNum: Number, ack: Number): ByteArray {
        val payload = byteArrayOf(data.packetId, seqNum.toByte(), ack.toByte(), *data.payload)
        check(payload.size == 64)
        return payload
    }

    private fun loop() {
        when (state) {
            States.waitFor0 -> {
                if (sendQueue.isNotEmpty()) {
                    val message = sendQueue.pop()
                    lastMessage = message
                    transportLayer.writeBytes(makePacketData(message, 0, 1))
                    state = States.waitForAck0
                }
            }

            States.waitForAck0 -> {
                try {
                    val message = transportLayer.readBytes()
                    if (message.ack == 0.toByte()) {
                        // This is the ACK we are looking for
                        receiveQueue.addLast(PacketMessage.fromBytes(message))

                        state = States.waitFor1

                        // Try to send another message right away
                        if (sendQueue.isNotEmpty()) {
                            loop()
                        }
                    }
                } catch (ex: SocketTimeoutException) {
                    // Timeout waiting for ACK0. Send again.
                    transportLayer.writeBytes(makePacketData(lastMessage, 0, 1))
                }
            }

            States.waitFor1 -> {
                if (sendQueue.isNotEmpty()) {
                    val message = sendQueue.pop()
                    lastMessage = message
                    transportLayer.writeBytes(makePacketData(message, 1, 0))
                    state = States.waitForAck1
                }
            }

            States.waitForAck1 -> {
                try {
                    val message = transportLayer.readBytes()
                    if (message.ack == 1.toByte()) {
                        // This is the ACK we are looking for
                        receiveQueue.addLast(PacketMessage.fromBytes(message))

                        state = States.waitFor0

                        // Try to send another message right away
                        if (sendQueue.isNotEmpty()) {
                            loop()
                        }
                    }
                } catch (ex: SocketTimeoutException) {
                    // Timeout waiting for ACK0. Send again.
                    transportLayer.writeBytes(makePacketData(lastMessage, 1, 0))
                }
            }
        }
    }
}
