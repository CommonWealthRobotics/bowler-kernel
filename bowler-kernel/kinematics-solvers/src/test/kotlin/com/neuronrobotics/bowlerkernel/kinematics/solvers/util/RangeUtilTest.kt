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

import com.google.common.math.DoubleMath
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class RangeUtilTest {

    @Test
    fun `test double range`() {
        val expected = setOf(1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9)
        val actual = 1.0..2.0 step 0.1

        assertEquals(actual.toList().size, actual.toSet().size)
        assertThat(actual.toSet(), hasSize(equalTo(expected.size)))

        val assertion = expected.zip(actual).fold(true) { acc, elem ->
            acc && DoubleMath.fuzzyEquals(elem.first, elem.second, 1e-8)
        }

        assertTrue(assertion)
    }
}
