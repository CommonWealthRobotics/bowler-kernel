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

import java.util.ArrayDeque

/**
 * @param sendQueue The queue to store packets to send in.
 * @param receiveQueue The queue to receive reply packets in.
 * @param state The initial RDT state.
 */
data class ReliableState(
    val sendQueue: ArrayDeque<PacketMessage> = ArrayDeque(),
    val receiveQueue: ArrayDeque<PacketMessage> = ArrayDeque(),
    var state: States = States.WaitFor0
) {
    lateinit var lastMessage: PacketMessage
}
