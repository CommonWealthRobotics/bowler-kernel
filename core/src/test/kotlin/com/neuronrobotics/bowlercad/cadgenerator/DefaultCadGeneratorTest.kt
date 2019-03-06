/*
 * This file is part of bowler-cad.
 *
 * bowler-cad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-cad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-cad.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlercad.cadgenerator

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import com.natpryce.hamkrest.hasSize
import com.neuronrobotics.bowlercad.createMockKinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import javafx.scene.paint.Color
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.octogonapus.guavautil.collections.emptyImmutableList
import org.octogonapus.guavautil.collections.immutableListOf
import kotlin.test.assertEquals

internal class DefaultCadGeneratorTest {

    private val generator = DefaultCadGenerator(
        bodyThickness = 5.0,
        cuboidThickness = 5.0,
        lengthForParamZero = 5.0
    )

    @Test
    fun `test with zero dh params`() {
        val result = generator.generateLimbs(createMockKinematicBase(emptyImmutableList()))

        assertThat(result.values(), hasSize(equalTo(0)))
    }

    @Test
    fun `test with one dh param`() {
        val result = generator.generateLimbs(
            createMockKinematicBase(
                immutableListOf(
                    immutableListOf(DhParam(11, 0, 12, 0))
                )
            )
        )

        assertAll(
            { assertThat(result.values(), hasSize(equalTo(1))) },
            { assertThat(result.values().first(), hasSize(equalTo(2))) },
            {
                // Check for green cuboid for d
                assertThat(
                    result.values().first().map { it.color },
                    hasElement(Color.GREEN)
                )
            },
            {
                // Check for red cuboid for r
                assertThat(
                    result.values().first().map { it.color },
                    hasElement(Color.RED)
                )
            },
            {
                // Check for cuboid with z length of 11 for d
                assertThat(
                    result.values().first().map { it.totalZ },
                    hasElement(11.0)
                )
            },
            {
                // Check for cuboid with x length of 12 for r
                assertThat(
                    result.values().first().map { it.totalX },
                    hasElement(12.0)
                )
            }
        )
    }

    @Test
    fun `test body`() {
        val result = generator.generateBody(createMockKinematicBase(emptyImmutableList()))

        assertAll(
            { assertEquals(5.0, result.totalX) },
            { assertEquals(5.0, result.totalY) },
            { assertEquals(5.0, result.totalZ) }
        )
    }
}
