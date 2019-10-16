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
package com.neuronrobotics.bowlerkernel.kinematics.solvers.util

import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class MathUtilTest {

    @ParameterizedTest
    @MethodSource("modTestSource")
    fun `modulus test double double`(lhs: Double, rhs: Double, expected: Double) {
        assertEquals(expected, lhs.modulus(rhs))
    }

    @ParameterizedTest
    @MethodSource("modTestSource")
    fun `modulus test int double`(lhs: Int, rhs: Double, expected: Double) {
        assertEquals(expected, lhs.modulus(rhs))
    }

    @ParameterizedTest
    @MethodSource("modTestSource")
    fun `modulus test double int`(lhs: Double, rhs: Int, expected: Double) {
        assertEquals(expected, lhs.modulus(rhs))
    }

    @ParameterizedTest
    @MethodSource("modTestSource")
    fun `modulus test int int`(lhs: Int, rhs: Int, expected: Int) {
        assertEquals(expected, lhs.modulus(rhs))
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun modTestSource() = listOf(
            Arguments.of(1, 2, 1),
            Arguments.of(-1, 2, 1)
        )
    }
}
