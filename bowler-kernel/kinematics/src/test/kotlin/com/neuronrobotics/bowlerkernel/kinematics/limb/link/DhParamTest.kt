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
package com.neuronrobotics.bowlerkernel.kinematics.limb.link

import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.getRotationMatrix
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.math.hypot

internal class DhParamTest {

    private val tolerance = 1e-14

    @Test
    fun `test theta 90`() {
        assertTrue(
            FrameTransformation.fromRotation(getRotationMatrix(90, 0, 0)).approxEquals(
                DhParam(0, 90, 0, 0).frameTransformation,
                tolerance
            )
        )
    }

    @Test
    fun `test alpha 90`() {
        assertTrue(
            FrameTransformation.fromRotation(getRotationMatrix(0, 0, 90)).approxEquals(
                DhParam(0, 0, 0, 90).frameTransformation,
                tolerance
            )
        )
    }

    @ParameterizedTest
    @MethodSource("lengthIndependentOfRotationSource")
    fun `test length independent of rotation`(theta: Double, alpha: Double) {
        assertEquals(
            hypot(2.0, 3.0), DhParam(
                2.0,
                theta,
                3.0,
                alpha
            ).length
        )
    }

    @ParameterizedTest
    @MethodSource("angleSource")
    fun `test angle`(angle: Double) {
        assertEquals(
            angle, DhParam(
                2.0,
                angle,
                3.0,
                angle
            ).angle
        )
    }

    @Test
    fun `test toFrameTransform`() {
        val expected = FrameTransformation.fromTranslation(3, 0, 2) *
            FrameTransformation.fromRotation(getRotationMatrix(0, 0, 90))

        val actual = listOf(
            DhParam(2, 0, 0, 0),
            DhParam(0, 0, 3, 90)
        ).toFrameTransformation()

        assertTrue(expected.approxEquals(actual, tolerance))
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun lengthIndependentOfRotationSource() =
            (0..90 step 10).map { Arguments.of(90 - it, it) }

        @Suppress("unused")
        @JvmStatic
        fun angleSource() = (0..90 step 10).toList()
    }
}
