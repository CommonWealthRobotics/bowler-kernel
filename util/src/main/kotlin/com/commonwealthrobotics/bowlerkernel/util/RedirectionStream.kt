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
package com.commonwealthrobotics.bowlerkernel.util

import java.io.OutputStream

/**
 * Redirects the data written into this [OutputStream] into the [readBuffer] lambda when the buffer is flushed.
 *
 * @param bufferSize The number of bytes the internal buffer can hold.
 * @param readBuffer A lambda that will be given a cope of the internal buffer when the buffer is flushed.
 */
class RedirectionStream(
    bufferSize: Int,
    private val readBuffer: (ByteArray) -> Unit
) : OutputStream() {

    private val data = ByteArray(bufferSize)
    private var i = 0

    override fun write(b: Int) {
        data[i] = b.toByte()
        if (i == data.size - 1) {
            flush()
        } else {
            i++
        }
    }

    override fun flush() {
        readBuffer(data.copyOf())
        i = 0
    }
}
