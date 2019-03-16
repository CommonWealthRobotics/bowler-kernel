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

import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice
import org.junit.jupiter.api.fail
import java.util.ArrayDeque

internal class MockDevice : AbstractSimpleComsDevice() {

    val writesReceived = ArrayDeque<ByteArray>()
    val readsToSend = ArrayDeque<ByteArray>()
    var canSendNextRead = false

    override fun write(message: ByteArray?, length: Int, howLongToWaitBeforeTimeout: Int) = 0

    override fun connectDeviceImp() = true

    override fun read(message: ByteArray?, howLongToWaitBeforeTimeout: Int) = 0

    override fun disconnectDeviceImp() = true

    override fun writeBytes(id: Int, values: ByteArray) {
        writesReceived.addLast(values.padToLength())
        canSendNextRead = true
    }

    @SuppressWarnings("ArrayPrimitive")
    override fun writeBytes(id: Int, values: Array<Byte>) = fail {
        "SimplePacketComs should not use this method!"
    }

    override fun readBytes(id: Int) = fail {
        "SimplePacketComs should not use this method!"
    }

    override fun readBytes(id: Int, values: ByteArray) {
        if (canSendNextRead) {
            canSendNextRead = false
            readsToSend.removeFirst().forEachIndexed { index, byte ->
                values[index] = byte
            }
        }
    }

    private fun ByteArray.padToLength() =
        this + (1..60 - size).map { 0.toByte() }.toByteArray()
}
