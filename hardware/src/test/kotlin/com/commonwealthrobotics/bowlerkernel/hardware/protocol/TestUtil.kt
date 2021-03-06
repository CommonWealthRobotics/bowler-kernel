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

import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceType
import io.kotest.matchers.collections.shouldContainExactly

private data class EqualsByteArray(val byteArray: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EqualsByteArray

        if (!byteArray.contentEquals(other.byteArray)) return false

        return true
    }

    override fun hashCode(): Int {
        return byteArray.contentHashCode()
    }
}

val digitalIn = ResourceType(1, 0, 1)
val digitalOut = ResourceType(2, 1, 0)
val analogIn = ResourceType(3, 0, 2)
val analogOut = ResourceType(4, 2, 0)
val serial = ResourceType(5, 60, 60)
val servo = ResourceType(6, 2, 0)
val servoWithFeedback = ResourceType(7, 2, 4)

fun Collection<ByteArray>?.shouldContainPayloads(vararg expected: ByteArray) =
    this?.map { EqualsByteArray(it) } shouldContainExactly expected.map { EqualsByteArray(it) }

fun ByteArray.shouldBe(expected: ByteArray) =
    EqualsByteArray(this) == EqualsByteArray(expected)
