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

import java.net.SocketTimeoutException

class DefaultDeviceServer(
    private val transportLayer: TransportLayer,
    private val payloadSize: Int
) : DeviceServer {

    private val packets = mutableMapOf<Byte, PacketTransferMode>()
    private val reliableState = mutableMapOf<Byte, ReliableState>()
    private val unreliableState = mutableMapOf<Byte, UnreliableState>()

    private fun ensureServerManagementPacket() {
        if (!packets.containsKey(DeviceServer.SERVER_MANAGEMENT_PACKET_ID)) {
            addReliable(DeviceServer.SERVER_MANAGEMENT_PACKET_ID)
        }
    }

    override fun connect() {
        transportLayer.connect()
        ensureServerManagementPacket()

        val response = write(
            DeviceServer.SERVER_MANAGEMENT_PACKET_ID,
            getPayload(payloadSize, byteArrayOf(DeviceServer.OPERATION_ADD_ENSURED_PACKETS))
        )

        if (response[0] != DeviceServer.STATUS_ACCEPTED) {
            throw IllegalStateException("The device failed to add the ensured packets: ${response.joinToString()}")
        }
    }

    override fun disconnect() {
        ensureServerManagementPacket()

        // Tell the server to discard all packet handlers and disconnect so the connection state
        // is correctly reset for RDT
        val response = write(
            DeviceServer.SERVER_MANAGEMENT_PACKET_ID,
            getPayload(payloadSize, byteArrayOf(DeviceServer.OPERATION_DISCONNECT_ID))
        )

        if (response[0] == DeviceServer.STATUS_ACCEPTED) {
            transportLayer.disconnect()
            packets.clear()
            reliableState.clear()
            unreliableState.clear()
            ensureServerManagementPacket()
        } else {
            throw IllegalStateException("The device failed to accept disconnection: ${response.joinToString()}")
        }
    }

    override fun addReliable(id: Byte) {
        packets[id] = PacketTransferMode.Reliable
        reliableState[id] = ReliableState()
    }

    override fun addUnreliable(id: Byte, maxRetries: Int) {
        packets[id] = PacketTransferMode.Unreliable
        unreliableState[id] = UnreliableState(maxRetries)
    }

    override fun write(id: Byte, payload: ByteArray): ByteArray = when (packets[id]) {
        PacketTransferMode.Reliable -> sendReceiveReliable(id, payload)

        PacketTransferMode.Unreliable -> sendReceiveUnreliable(id, payload, unreliableState[id]!!.maxRetries)

        null -> throw IllegalArgumentException("Cannot write to unregistered packet with id $id.")
    }

    /**
     * Send the packet and wait for a response using reliable transport.
     *
     * @param id The id of the packet.
     * @param payload The payload to send.
     * @return The reply packet.
     */
    internal fun sendReceiveReliable(id: Byte, payload: ByteArray): ByteArray {
        val state = reliableState[id]!!
        state.sendQueue.addLast(PacketMessage(id, payload))

        while (state.receiveQueue.isEmpty()) {
            loop(state)
        }

        return state.receiveQueue.pop().payload
    }

    /**
     * Send the packet and wait for a response using unreliable transport.
     *
     * @param id The id of the packet.
     * @param payload The payload to send.
     * @return The reply packet.
     */
    internal fun sendReceiveUnreliable(id: Byte, payload: ByteArray, maxRetries: Int): ByteArray {
        transportLayer.writeBytes(makePacketData(id, 0, 0, payload))
        return try {
            transportLayer.readBytes().let {
                // Take out the header
                it.sliceArray(3 until it.size)
            }
        } catch (ex: SocketTimeoutException) {
            check(maxRetries > 0)
            sendReceiveUnreliable(id, payload, maxRetries - 1)
        }
    }

    private fun loop(state: ReliableState) {
        when (state.state) {
            States.WaitFor0 -> {
                if (state.sendQueue.isNotEmpty()) {
                    val message = state.sendQueue.pop()
                    state.lastMessage = message
                    transportLayer.writeBytes(
                        makePacketData(
                            message.packetId,
                            0,
                            1,
                            message.payload
                        )
                    )
                    state.state = States.WaitForAck0
                }
            }

            States.WaitForAck0 -> {
                try {
                    val message = transportLayer.readBytes()
                    if (message.ack == 0.toByte()) {
                        // This is the ACK we are looking for
                        state.receiveQueue.addLast(PacketMessage.fromBytes(message))

                        state.state = States.WaitFor1

                        // Try to send another message right away
                        if (state.sendQueue.isNotEmpty()) {
                            loop(state)
                        }
                    }
                } catch (ex: SocketTimeoutException) {
                    // Timeout waiting for ACK0. Send again.
                    transportLayer.writeBytes(
                        makePacketData(
                            state.lastMessage.packetId,
                            0,
                            1,
                            state.lastMessage.payload
                        )
                    )
                }
            }

            States.WaitFor1 -> {
                if (state.sendQueue.isNotEmpty()) {
                    val message = state.sendQueue.pop()
                    state.lastMessage = message
                    transportLayer.writeBytes(
                        makePacketData(
                            message.packetId,
                            1,
                            0,
                            message.payload
                        )
                    )
                    state.state = States.WaitForAck1
                }
            }

            States.WaitForAck1 -> {
                try {
                    val message = transportLayer.readBytes()
                    if (message.ack == 1.toByte()) {
                        // This is the ACK we are looking for
                        state.receiveQueue.addLast(PacketMessage.fromBytes(message))

                        state.state = States.WaitFor0

                        // Try to send another message right away
                        if (state.sendQueue.isNotEmpty()) {
                            loop(state)
                        }
                    }
                } catch (ex: SocketTimeoutException) {
                    // Timeout waiting for ACK0. Send again.
                    transportLayer.writeBytes(
                        makePacketData(
                            state.lastMessage.packetId,
                            1,
                            0,
                            state.lastMessage.payload
                        )
                    )
                }
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun makePacketData(id: Byte, seqNum: Byte, ack: Byte, payload: ByteArray) =
        byteArrayOf(id, seqNum, ack) + payload
}
