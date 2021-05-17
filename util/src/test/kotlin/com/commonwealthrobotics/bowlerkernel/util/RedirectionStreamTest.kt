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

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

internal class RedirectionStreamTest {

    @Test
    fun `read chunks`() {
        val chunks = mutableListOf<ByteArray>()
        val stream = RedirectionStream(32) {
            chunks.add(it)
        }

        for (i in 1..64) {
            stream.write(i)
        }

        assertArrayEquals((1..32).map { it.toByte() }.toByteArray(), chunks[0])
        assertArrayEquals((33..64).map { it.toByte() }.toByteArray(), chunks[1])
    }
}
