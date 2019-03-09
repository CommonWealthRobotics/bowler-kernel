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
package com.neuronrobotics.bowlerkernel.hardware.protocol

import arrow.core.Either
import arrow.core.Option
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice
import edu.wpi.SimplePacketComs.BytePacketType
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

/**
 * An implementation of [BowlerRPCProtocol] using SimplePacketComs. Uses a continuous range of
 * packet ids from [getLowestPacketId] through [getHighestPacketId]. Any numbers outside that
 * range are available for other packets.
 *
 * @param comms The comms implementation.
 * @param startPacketId The starting range of the packets this class creates.
 */
@SuppressWarnings("TooManyFunctions")
class SimplePacketComsProtocol(
    private val comms: AbstractSimpleComsDevice,
    private val startPacketId: Int = DISCOVERY_PACKET_ID + 1
) {

    private var highestPacketId = AtomicInteger(startPacketId)

    /**
     * Whether the device is connected.
     */
    private var isConnected = false

    private val discoveryPacket = BytePacketType(DISCOVERY_PACKET_ID, PACKET_SIZE).apply {
        waitToSendMode()
    }

    private lateinit var discoveryData: ByteArray
    private lateinit var discoveryLatch: CountDownLatch

    init {
        require(startPacketId > 0) {
            "The starting packet id ($startPacketId) must be greater than zero."
        }

        require(startPacketId != DISCOVERY_PACKET_ID) {
            "The starting packet id ($startPacketId) cannot be equal to the discovery packet id " +
                "($DISCOVERY_PACKET_ID)."
        }

        comms.addPollingPacket(discoveryPacket)

        comms.addEvent(DISCOVERY_PACKET_ID) {
            discoveryData = comms.readBytes(DISCOVERY_PACKET_ID).toByteArray()
            discoveryLatch.countDown()
        }

        comms.addTimeout(DISCOVERY_PACKET_ID) { discoveryPacket.oneShotMode() }
    }

    private fun validateConnection() {
        if (!isConnected) {
            throw IllegalStateException("The RPC is not connected.")
        }
    }

    /**
     * Sends a general discovery packet.
     *
     * Section 2.1.1.
     *
     * @param operation The operation.
     * @param payload The payload.
     * @return The payload if accepted, the status code if rejected.
     */
    private fun sendGeneralDiscoveryPacket(
        operation: Byte,
        payload: ByteArray
    ): Either<Byte, ByteArray> {
        validateConnection()

        discoveryLatch = CountDownLatch(1)
        comms.writeBytes(DISCOVERY_PACKET_ID, byteArrayOf(operation) + payload)
        discoveryPacket.oneShotMode()
        discoveryLatch.await()

        println(
            """
            |Discovery response:
            |${discoveryData.joinToString()}
            """.trimMargin()
        )

        val status = discoveryData[0]
        return if (status == STATUS_ACCEPTED) {
            discoveryData.right()
        } else {
            status.left()
        }
    }

    /**
     * Sends a discovery packet.
     *
     * Section 2.1.2.
     *
     * @param packetId The new ID for the packet being discovered.
     * @param resource The type of the resource.
     * @param attachment The type of the attachment point.
     * @param attachmentData Any data needed to fully describe the attachment.
     * @return [Option.empty] if accepted, the status code if rejected.
     */
    private fun sendDiscoveryPacket(
        packetId: Byte,
        resource: Byte,
        attachment: Byte,
        attachmentData: ByteArray
    ): Option<Byte> =
        sendGeneralDiscoveryPacket(
            OPERATION_DISCOVERY_ID,
            byteArrayOf(packetId, resource, attachment) + attachmentData
        ).swap().toOption()

    /**
     * Sends a group discovery packet.
     *
     * Section 2.1.3.
     *
     * @param groupId The ID for the group being made.
     * @param packetId The ID for the packet the group will use.
     * @param count The number of resources that will be added to the group.
     * @return [Option.empty] if accepted, the status code if rejected.
     */
    private fun sendGroupDiscoveryPacket(
        groupId: Byte,
        packetId: Byte,
        count: Byte
    ): Option<Byte> =
        sendGeneralDiscoveryPacket(
            OPERATION_GROUP_DISCOVERY_ID,
            byteArrayOf(groupId, packetId, count)
        ).swap().toOption()

    /**
     * Sends a group member discovery packet.
     *
     * Section 2.1.4.
     *
     * @param groupId The ID for the group that this resource will be added to.
     * @param sendStart The starting byte index in the send payload for this resource's write data.
     * @param sendEnd The ending byte index in the send payload for this resource's write data.
     * @param receiveStart The starting byte index in the receive payload for this resource's
     * read data.
     * @param receiveEnd The ending byte index in the receive payload for this resource's read data.
     * @param resource The type of the resource.
     * @param attachment The type of the attachment point.
     * @param attachmentData Any data needed to fully describe the attachment.
     * @return [Option.empty] if accepted, the status code if rejected.
     */
    private fun sendGroupMemberDiscoveryPacket(
        groupId: Byte,
        sendStart: Byte,
        sendEnd: Byte,
        receiveStart: Byte,
        receiveEnd: Byte,
        resource: Byte,
        attachment: Byte,
        attachmentData: ByteArray
    ): Option<Byte> =
        sendGeneralDiscoveryPacket(
            OPERATION_GROUP_MEMBER_DISCOVERY_ID,
            byteArrayOf(
                groupId, sendStart, sendEnd, receiveStart, receiveEnd, resource, attachment
            ) + attachmentData
        ).swap().toOption()

    /**
     * Sends a discard discovery packet.
     *
     * Section 2.1.5.
     *
     * @return The status code.
     */
    private fun sendDiscardDiscoveryPacket(): Byte =
        sendGeneralDiscoveryPacket(
            OPERATION_DISCARD_DISCOVERY_ID, byteArrayOf()
        ).swap().getOrHandle { it[0] }

    /**
     * The lowest packet id.
     */
    fun getLowestPacketId(): Int = startPacketId

    /**
     * The highest packet id.
     */
    fun getHighestPacketId(): Int = highestPacketId.get()

    companion object {

        /**
         * The id of the discovery packet.
         */
        const val DISCOVERY_PACKET_ID = 1

        /**
         * The maximum size of a packet payload in bytes.
         */
        const val PAYLOAD_SIZE = 60

        /**
         * The size of a packet in bytes.
         */
        const val PACKET_SIZE = PAYLOAD_SIZE + 4

        /**
         * The operation ID's.
         */
        private const val OPERATION_DISCOVERY_ID = 1.toByte()
        private const val OPERATION_GROUP_DISCOVERY_ID = 2.toByte()
        private const val OPERATION_GROUP_MEMBER_DISCOVERY_ID = 3.toByte()
        private const val OPERATION_DISCARD_DISCOVERY_ID = 4.toByte()

        /**
         * The status codes.
         */
        private const val STATUS_ACCEPTED = 1.toByte()
        private const val STATUS_REJECTED_GENERIC = 2.toByte()
        private const val STATUS_REJECTED_UNKNOWN_RESOURCE = 3.toByte()
        private const val STATUS_REJECTED_UNKNOWN_ATTACHMENT = 4.toByte()
        private const val STATUS_REJECTED_INVALID_ATTACHMENT = 5.toByte()
        private const val STATUS_REJECTED_INVALID_ATTACHMENT_DATA = 6.toByte()
        private const val STATUS_REJECTED_INVALID_GROUP_ID = 7.toByte()
        private const val STATUS_REJECTED_GROUP_FULL = 8.toByte()
        private const val STATUS_REJECTED_UNKNOWN_OPERATION = 9.toByte()
        private const val STATUS_DISCARD_IN_PROGRESS = 10.toByte()
        private const val STATUS_DISCARD_COMPLETE = 11.toByte()
    }
}
