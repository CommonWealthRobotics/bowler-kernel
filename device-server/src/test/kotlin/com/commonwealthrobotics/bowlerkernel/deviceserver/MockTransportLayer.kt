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

import java.util.ArrayDeque

class MockTransportLayer : TransportLayer {

    private var connected = false
    val reads = ArrayDeque<ByteArray>()
    val writes = ArrayDeque<ByteArray>()

    override fun connect() {
        connected = true
    }

    override fun disconnect() {
        connected = false
    }

    override fun readBytes(): ByteArray {
        check(connected)
        return reads.pop()
    }

    override fun writeBytes(data: ByteArray) {
        check(connected)
        writes.addLast(data)
    }
}
