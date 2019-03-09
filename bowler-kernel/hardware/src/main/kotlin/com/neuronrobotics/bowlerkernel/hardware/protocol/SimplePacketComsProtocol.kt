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
import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice
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
     * Sends a general discovery packet.
     *
     * Section 2.1.1.
     *
     * @param operation The operation.
     * @return The payload if accepted, the status code if rejected.
     */
    private fun sendGeneralDiscoveryPacket(operation: Byte): Either<Byte, ByteArray> {
        TODO()
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
    ): Option<Byte> {
        TODO()
    }

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
    ): Option<Byte> {
        TODO()
    }

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
    ): Option<Byte> {
        TODO()
    }

    /**
     * Sends a discard discovery packet.
     *
     * Section 2.1.5.
     *
     * @return The status code.
     */
    private fun sendDiscardDiscoveryPacket(): Byte {
        TODO()
    }

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
        private const val OPERATION_DISCOVERY_ID = 1
        private const val OPERATION_GROUP_DISCOVERY_ID = 2
        private const val OPERATION_GROUP_MEMBER_DISCOVERY_ID = 3
        private const val OPERATION_DISCARD_DISCOVERY_ID = 4

        /**
         * The status codes.
         */
        private const val STATUS_ACCEPTED = 1
        private const val STATUS_REJECTED_GENERIC = 2
        private const val STATUS_REJECTED_UNKNOWN_RESOURCE = 3
        private const val STATUS_REJECTED_UNKNOWN_ATTACHMENT = 4
        private const val STATUS_REJECTED_INVALID_ATTACHMENT = 5
        private const val STATUS_REJECTED_INVALID_ATTACHMENT_DATA = 6
        private const val STATUS_REJECTED_INVALID_GROUP_ID = 7
        private const val STATUS_REJECTED_GROUP_FULL = 8
        private const val STATUS_REJECTED_UNKNOWN_OPERATION = 9
        private const val STATUS_DISCARD_IN_PROGRESS = 10
        private const val STATUS_DISCARD_COMPLETE = 11
    }
}
