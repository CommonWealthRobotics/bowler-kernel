/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.dhparam

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.math.sqrt
import kotlin.random.Random

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
