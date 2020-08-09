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
package com.commonwealthrobotics.bowlerkernel.hardware.protocol

import com.commonwealthrobotics.bowlerkernel.deviceserver.DeviceServer
import com.commonwealthrobotics.bowlerkernel.deviceserver.getPayload
import java.util.ArrayDeque

internal class MockDeviceServer : DeviceServer {

    val reads = ArrayDeque<ByteArray>()
    val writes = ArrayDeque<Pair<Byte, ByteArray>>()

    @SuppressWarnings("EmptyFunctionBlock")
    override fun connect() {
    }

    @SuppressWarnings("EmptyFunctionBlock")
    override fun disconnect() {
    }

    @SuppressWarnings("EmptyFunctionBlock")
    override fun addReliable(id: Byte) {
    }

    @SuppressWarnings("EmptyFunctionBlock")
    override fun addUnreliable(id: Byte, maxRetries: Int) {
    }

    override fun write(id: Byte, payload: ByteArray): ByteArray {
        writes.addLast(id to payload)
        // The real device will report zeros if there is no packet
        return if (reads.isEmpty()) {
            getPayload(DefaultBowlerRPCProtocol.PAYLOAD_SIZE)
        } else {
            reads.pop()
        }
    }
}
