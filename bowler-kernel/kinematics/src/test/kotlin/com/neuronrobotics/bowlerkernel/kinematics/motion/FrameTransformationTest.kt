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
package com.neuronrobotics.bowlerkernel.kinematics.motion

import org.ejml.simple.SimpleMatrix
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.lang.Math.toDegrees
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal class FrameTransformationTest {

    // Used to check for SimpleMatrix equality
    private val equalityTolerance = 1e-8

    @Test
    fun `test identity`() {
        assertEquals(
            FrameTransformation.fromSimpleMatrix(
                SimpleMatrix.identity(4)
            ),
            FrameTransformation.identity()
        )
    }

    @Test
    fun `test fromTranslation with xyz and getTranslation`() {
        val expected = FrameTransformation.fromSimpleMatrix(
            SimpleMatrix.identity(4).apply {
                this[0, 3] = 1.0
                this[1, 3] = 2.0
                this[2, 3] = 3.0
            }
        )

        val tested = FrameTransformation.fromTranslation(1, 2, 3)

        assertAll(
            { assertEquals(expected, tested) },
            {
                assertTrue(
                    SimpleMatrix(3, 1).apply {
                        this[0, 0] = 1.0
                        this[1, 0] = 2.0
                        this[2, 0] = 3.0
                    }.isIdentical(tested.getTranslation(), equalityTolerance)
                )
            }
        )
    }

    @Test
    fun `test fromTranslation with SimpleMatrix`() {
        val expected = FrameTransformation.fromSimpleMatrix(
            SimpleMatrix.identity(4).apply {
                this[0, 3] = 1.0
                this[1, 3] = 2.0
                this[2, 3] = 3.0
            }
        )

        val testedMat = SimpleMatrix(3, 1).apply {
            this[0, 0] = 1.0
            this[1, 0] = 2.0
            this[2, 0] = 3.0
        }

        val tested = FrameTransformation.fromTranslation(testedMat)

        assertAll(
            { assertEquals(expected, tested) },
            { assertTrue(testedMat.isIdentical(tested.getTranslation(), equalityTolerance)) }
        )
    }

    @Test
    fun `test fromRotation around z`() {
        val angle = Math.PI / 2.0
        val cosAngle = cos(angle)
        val sinAngle = sin(angle)

        val expected = FrameTransformation.fromSimpleMatrix(
            SimpleMatrix.identity(4).apply {
                this[0, 0] = cosAngle
                this[1, 0] = sinAngle
                this[0, 1] = -sinAngle
                this[1, 1] = cosAngle
            }
        )

        val tested = FrameTransformation.fromRotation(0, 0, toDegrees(angle))

        assertAll(
            { assertEquals(expected, tested) },
            {
                SimpleMatrix.identity(3).apply {
                    this[0, 0] = cosAngle
                    this[1, 0] = sinAngle
                    this[0, 1] = -sinAngle
                    this[1, 1] = cosAngle
                }.isIdentical(tested.getRotation(), equalityTolerance)
            }
        )
    }

    @Test
    fun `test fromRotation around y`() {
        val angle = Math.PI / 2.0
        val cosAngle = cos(angle)
        val sinAngle = sin(angle)

        val expected = FrameTransformation.fromSimpleMatrix(
            SimpleMatrix.identity(4).apply {
                this[0, 0] = cosAngle
                this[2, 0] = -sinAngle
                this[0, 2] = sinAngle
                this[2, 2] = cosAngle
            }
        )

        val tested = FrameTransformation.fromRotation(0, toDegrees(angle), 0)

        assertAll(
            { assertEquals(expected, tested) },
            {
                SimpleMatrix.identity(3).apply {
                    this[0, 0] = cosAngle
                    this[2, 0] = -sinAngle
                    this[0, 2] = sinAngle
                    this[2, 2] = cosAngle
                }.isIdentical(tested.getRotation(), equalityTolerance)
            }
        )
    }

    @Test
    fun `test fromRotation around x`() {
        val angle = Math.PI / 2.0
        val cosAngle = cos(angle)
        val sinAngle = sin(angle)

        val expected = FrameTransformation.fromSimpleMatrix(
            SimpleMatrix.identity(4).apply {
                this[1, 1] = cosAngle
                this[2, 1] = sinAngle
                this[1, 2] = -sinAngle
                this[2, 2] = cosAngle
            }
        )

        val tested = FrameTransformation.fromRotation(toDegrees(angle), 0, 0)

        assertAll(
            { assertEquals(expected, tested) },
            {
                SimpleMatrix.identity(3).apply {
                    this[1, 1] = cosAngle
                    this[2, 1] = sinAngle
                    this[1, 2] = -sinAngle
                    this[2, 2] = cosAngle
                }.isIdentical(tested.getRotation(), equalityTolerance)
            }
        )
    }

    @Test
    fun `test fromRotation with SimpleMatrix`() {
        fun SimpleMatrix.populate() = apply {
            for (row in 0 until 3) {
                for (col in 0 until 3) {
                    this[row, col] = (row + col).toDouble()
                }
            }
        }

        val expected = FrameTransformation.fromSimpleMatrix(SimpleMatrix.identity(4).populate())
        val tested = FrameTransformation.fromRotation(SimpleMatrix(3, 3).populate())

        assertAll(
            { assertEquals(expected, tested) },
            {
                SimpleMatrix(3, 3).populate().isIdentical(tested.getRotation(), equalityTolerance)
            }
        )
    }

    @Test
    fun `test length with column vector`() {
        assertEquals(
            sqrt(1.0.pow(2) + 2.0.pow(2) + 3.0.pow(2)),
            SimpleMatrix(3, 1).apply {
                this[0, 0] = 1.0
                this[1, 0] = 2.0
                this[2, 0] = 3.0
            }.length()
        )
    }

    @Test
    fun `test length with row vector`() {
        assertEquals(
            sqrt(1.0.pow(2) + 2.0.pow(2) + 3.0.pow(2)),
            SimpleMatrix(1, 3).apply {
                this[0, 0] = 1.0
                this[0, 1] = 2.0
                this[0, 2] = 3.0
            }.length()
        )
    }
}