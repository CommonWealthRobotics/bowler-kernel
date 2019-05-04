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
@file:SuppressWarnings("LargeClass")

package com.neuronrobotics.bowlerkernel.kinematics.motion

import com.beust.klaxon.Klaxon
import org.ejml.simple.SimpleMatrix
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.lang.Math.toDegrees
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal class FrameTransformationTest {

    // Used to check for SimpleMatrix equality
    private val equalityTolerance = 1e-14

    @Test
    fun `test identity`() {
        assertEquals(
            FrameTransformation.fromSimpleMatrix(
                SimpleMatrix.identity(4)
            ),
            FrameTransformation.identity
        )
    }

    @Test
    fun `test fromTranslation with xyz and getTranslation`() {
        val expected =
            FrameTransformation.fromSimpleMatrix(
                SimpleMatrix.identity(4).apply {
                    this[0, 3] = 1.0
                    this[1, 3] = 2.0
                    this[2, 3] = 3.0
                }
            )

        val tested =
            FrameTransformation.fromTranslation(
                1,
                2,
                3
            )

        assertAll(
            { assertEquals(expected, tested) },
            {
                assertTrue(
                    SimpleMatrix(3, 1).apply {
                        this[0, 0] = 1.0
                        this[1, 0] = 2.0
                        this[2, 0] = 3.0
                    }.isIdentical(tested.translation, equalityTolerance)
                )
            }
        )
    }

    @Test
    fun `test fromTranslation with SimpleMatrix`() {
        val expected =
            FrameTransformation.fromSimpleMatrix(
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

        val tested =
            FrameTransformation.fromTranslation(
                testedMat
            )

        assertAll(
            { assertEquals(expected, tested) },
            { assertTrue(testedMat.isIdentical(tested.translation, equalityTolerance)) }
        )
    }

    @Test
    fun `test fromRotation around z`() {
        val angle = PI / 2.0
        val cosAngle = cos(angle)
        val sinAngle = sin(angle)

        val expected =
            FrameTransformation.fromSimpleMatrix(
                SimpleMatrix.identity(4).apply {
                    this[0, 0] = cosAngle
                    this[1, 0] = sinAngle
                    this[0, 1] = -sinAngle
                    this[1, 1] = cosAngle
                }
            )

        val tested =
            FrameTransformation.fromRotation(
                0,
                0,
                toDegrees(angle)
            )

        assertAll(
            { assertEquals(expected, tested) },
            {
                assertTrue(
                    SimpleMatrix.identity(3).apply {
                        this[0, 0] = cosAngle
                        this[1, 0] = sinAngle
                        this[0, 1] = -sinAngle
                        this[1, 1] = cosAngle
                    }.isIdentical(tested.rotation, equalityTolerance)
                )
            }
        )
    }

    @Test
    fun `test fromRotation around y`() {
        val angle = PI / 2.0
        val cosAngle = cos(angle)
        val sinAngle = sin(angle)

        val expected =
            FrameTransformation.fromSimpleMatrix(
                SimpleMatrix.identity(4).apply {
                    this[0, 0] = cosAngle
                    this[2, 0] = -sinAngle
                    this[0, 2] = sinAngle
                    this[2, 2] = cosAngle
                }
            )

        val tested =
            FrameTransformation.fromRotation(
                0,
                toDegrees(angle),
                0
            )

        assertAll(
            { assertEquals(expected, tested) },
            {
                assertTrue(
                    SimpleMatrix.identity(3).apply {
                        this[0, 0] = cosAngle
                        this[2, 0] = -sinAngle
                        this[0, 2] = sinAngle
                        this[2, 2] = cosAngle
                    }.isIdentical(tested.rotation, equalityTolerance)
                )
            }
        )
    }

    @Test
    fun `test fromRotation around x`() {
        val angle = PI / 2.0
        val cosAngle = cos(angle)
        val sinAngle = sin(angle)

        val expected =
            FrameTransformation.fromSimpleMatrix(
                SimpleMatrix.identity(4).apply {
                    this[1, 1] = cosAngle
                    this[2, 1] = sinAngle
                    this[1, 2] = -sinAngle
                    this[2, 2] = cosAngle
                }
            )

        val tested =
            FrameTransformation.fromRotation(
                toDegrees(angle),
                0,
                0
            )

        assertAll(
            { assertEquals(expected, tested) },
            {
                assertTrue(
                    SimpleMatrix.identity(3).apply {
                        this[1, 1] = cosAngle
                        this[2, 1] = sinAngle
                        this[1, 2] = -sinAngle
                        this[2, 2] = cosAngle
                    }.isIdentical(tested.rotation, equalityTolerance)
                )
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

        val expected =
            FrameTransformation.fromSimpleMatrix(
                SimpleMatrix.identity(4).populate()
            )
        val tested =
            FrameTransformation.fromRotation(
                SimpleMatrix(3, 3).populate()
            )

        assertAll(
            { assertEquals(expected, tested) },
            {
                assertTrue(
                    SimpleMatrix(3, 3).populate().isIdentical(
                        tested.rotation,
                        equalityTolerance
                    )
                )
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

    @Test
    fun `test getTranslationPlanar`() {
        val expected = SimpleMatrix(2, 1).apply {
            this[0] = 1.0
            this[1] = 2.0
        }

        val actual = FrameTransformation.fromTranslation(
            1,
            2,
            3
        ).translationPlanar

        assertTrue(expected.isIdentical(actual, equalityTolerance))
    }

    @Test
    fun `test getTranslationCol`() {
        val expected = SimpleMatrix(4, 1).apply {
            this[0] = 1.0
            this[1] = 2.0
            this[2] = 3.0
            this[3] = 1.0
        }

        val actual = FrameTransformation.fromTranslation(
            1,
            2,
            3
        ).translationCol

        assertTrue(expected.isIdentical(actual, equalityTolerance))
    }

    @Test
    fun `test subMatrix`() {
        fun SimpleMatrix.populate(rowOffset: Int = 0, colOffset: Int = 0) {
            this[rowOffset + 0, colOffset + 0] = 1.0
            this[rowOffset + 0, colOffset + 1] = 2.0
            this[rowOffset + 1, colOffset + 0] = 3.0
            this[rowOffset + 1, colOffset + 1] = 4.0
        }

        val expected = SimpleMatrix(2, 2).apply {
            populate()
        }

        val actual = FrameTransformation.fromSimpleMatrix(
            SimpleMatrix(4, 4).apply {
                populate(1, 1)
            }
        ).subMatrix(1, 3, 1, 3)

        assertTrue(expected.isIdentical(actual, equalityTolerance))
    }

    @Test
    fun `test json round trip`() {
        val expected =
            FrameTransformation.fromTranslation(
                1,
                2,
                3
            )
        val klaxon = Klaxon().converter(FrameTransformation.converter)
        val json = klaxon.toJsonString(expected)
        val ftFromJson = klaxon.parse<FrameTransformation>(json)
        assertEquals(expected, ftFromJson)
    }

    @Test
    fun `test getRotationMatrix around z only`() {
        val zRad = PI / 3

        val expected = SimpleMatrix.identity(3).apply {
            this[0, 0] = cos(zRad)
            this[0, 1] = -sin(zRad)
            this[1, 0] = sin(zRad)
            this[1, 1] = cos(zRad)
        }

        val actual = getRotationMatrix(
            0,
            0,
            toDegrees(zRad)
        )

        assertTrue(expected.isIdentical(actual, equalityTolerance))
    }

    @Test
    fun `test getRotationMatrix around y only`() {
        val yRad = PI / 3

        val expected = SimpleMatrix.identity(3).apply {
            this[0, 0] = cos(yRad)
            this[0, 2] = sin(yRad)
            this[2, 0] = -sin(yRad)
            this[2, 2] = cos(yRad)
        }

        val actual = getRotationMatrix(
            0,
            toDegrees(yRad),
            0
        )

        assertTrue(expected.isIdentical(actual, equalityTolerance))
    }

    @Test
    fun `test getRotationMatrix around x only`() {
        val xRad = PI / 3

        val expected = SimpleMatrix.identity(3).apply {
            this[1, 1] = cos(xRad)
            this[1, 2] = -sin(xRad)
            this[2, 1] = sin(xRad)
            this[2, 2] = cos(xRad)
        }

        val actual = getRotationMatrix(
            toDegrees(xRad),
            0,
            0
        )

        assertTrue(expected.isIdentical(actual, equalityTolerance))
    }

    @Test
    fun `test approxEquals with negative zero`() {
        assertTrue(
            FrameTransformation.fromSimpleMatrix(
                SimpleMatrix(4, 4).apply {
                    this[0, 0] = 0.0
                }).approxEquals(
                FrameTransformation.fromSimpleMatrix(
                    SimpleMatrix(4, 4).apply {
                        this[0, 0] = -0.0
                    }),
                equalityTolerance
            )
        )
    }

    @Test
    fun `test approxEquals with tolerance`() {
        assertTrue(
            FrameTransformation.fromSimpleMatrix(
                SimpleMatrix(4, 4).apply {
                    this[0, 0] = 0.0
                }).approxEquals(
                FrameTransformation.fromSimpleMatrix(
                    SimpleMatrix(4, 4).apply {
                        this[0, 0] = equalityTolerance
                    }),
                equalityTolerance
            )
        )
    }

    @Test
    fun `test approxEquals failure`() {
        assertFalse(
            FrameTransformation.fromSimpleMatrix(
                SimpleMatrix(4, 4).apply {
                    this[0, 0] = 0.0
                }).approxEquals(
                FrameTransformation.fromSimpleMatrix(
                    SimpleMatrix(4, 4).apply {
                        this[0, 0] = equalityTolerance + 1e-20
                    }),
                equalityTolerance
            )
        )
    }

    @Test
    fun `test translationX`() {
        assertEquals(
            9.0,
            FrameTransformation.fromTranslation(9, 0, 0).translationX
        )
    }

    @Test
    fun `test translationY`() {
        assertEquals(
            9.0,
            FrameTransformation.fromTranslation(0, 9, 0).translationY
        )
    }

    @Test
    fun `test translationZ`() {
        assertEquals(
            9.0,
            FrameTransformation.fromTranslation(0, 0, 9).translationZ
        )
    }
}
