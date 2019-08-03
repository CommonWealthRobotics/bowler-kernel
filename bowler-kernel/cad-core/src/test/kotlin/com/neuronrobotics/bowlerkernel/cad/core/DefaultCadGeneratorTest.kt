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
package com.neuronrobotics.bowlerkernel.cad.core

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableList

internal class DefaultCadGeneratorTest {

    private val generator = DefaultCadGenerator(
        bodyThickness = 5.0,
        cuboidThickness = 5.0,
        lengthForParamZero = 5.0
    )

    @Test
    fun `test with zero dh params`() {
        val result = generator.generateLimbs(
            createMockKinematicBase(
                emptyImmutableList()
            )
        )

        assertThat(result.values(), hasSize(equalTo(0)))
    }

    @Test
    fun `test body`() {
        val result = generator.generateBody(
            createMockKinematicBase(
                emptyImmutableList()
            )
        )

        assertAll(
            { assertEquals(5.0, result.totalX) },
            { assertEquals(5.0, result.totalY) },
            { assertEquals(5.0, result.totalZ) }
        )
    }
}
