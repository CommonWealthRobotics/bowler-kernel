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
package com.neuronrobotics.bowlerkernel.kinematics.solvers.dhparam

import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt
import kotlin.random.Random
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DhParamTest {

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10])
    fun `test length on d`(testValue: Int) {
        assertEquals(testValue.toDouble(), DhParam(testValue, 0, 0, 0).length)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10])
    fun `test length on r`(testValue: Int) {
        assertEquals(testValue.toDouble(), DhParam(0, 0, testValue, 0).length)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10])
    fun `test length on d and r with random theta and alpha`(testValue: Int) {
        assertEquals(
            sqrt(2.0) * testValue,
            DhParam(testValue, Random.nextDouble(90.0), testValue, Random.nextDouble(90.0)).length,
            1e-10
        )
    }
}
