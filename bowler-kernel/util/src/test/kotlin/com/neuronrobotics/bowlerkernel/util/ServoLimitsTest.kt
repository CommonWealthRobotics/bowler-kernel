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
package com.neuronrobotics.bowlerkernel.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ServoLimitsTest {

    @Test
    fun `compute joint limits with positive scale`() {
        val expected = JointLimits(180, 0)
        val actual = ServoLimits(180, 0, 0, 1).toJointLimits()
        assertEquals(expected, actual)
    }

    @Test
    fun `compute joint limits with positive scale and nonzero center`() {
        val expected = JointLimits(170, -10)
        val actual = ServoLimits(180, 0, 10, 1).toJointLimits()
        assertEquals(expected, actual)
    }

    @Test
    fun `compute joint limits with negative scale`() {
        val expected = JointLimits(180, 0)
        val actual = ServoLimits(180, 0, 0, -1).toJointLimits()
        assertEquals(expected, actual)
    }

    @Test
    fun `compute joint limits with negative scale and nonzero center`() {
        val expected = JointLimits(170, -10)
        val actual = ServoLimits(180, 0, 10, -1).toJointLimits()
        assertEquals(expected, actual)
    }
}
