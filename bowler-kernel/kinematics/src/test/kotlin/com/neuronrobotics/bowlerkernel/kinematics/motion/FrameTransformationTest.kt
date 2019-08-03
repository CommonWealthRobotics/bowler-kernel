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
@file:SuppressWarnings("LargeClass", "TooManyFunctions")

package com.neuronrobotics.bowlerkernel.kinematics.motion

import Jama.Matrix
import arrow.core.Either
import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.kinematics.testJsonConversion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.lang.Math.toDegrees
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal class FrameTransformationTest {

    private val equalityTolerance = 1e-14

    @Test
    fun `test identity`() {
        assertEquals(
            FrameTransformation.fromMatrix(Matrix.identity(4, 4)),
            FrameTransformation.identity
        )
    }

    @Test
    fun `test fromTranslation with xyz and getTranslation`() {
        val expected =
            FrameTransformation.fromMatrix(
                Matrix.identity(4, 4).apply {
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
                    Matrix(3, 1).apply {
                        this[0, 0] = 1.0
                        this[1, 0] = 2.0
                        this[2, 0] = 3.0
                    }.approxEquals(tested.translation, equalityTolerance)
                )
            }
        )
    }

    @Test
    fun `test fromTranslation with Matrix`() {
        val expected =
            FrameTransformation.fromMatrix(
                Matrix.identity(4, 4).apply {
                    this[0, 3] = 1.0
                    this[1, 3] = 2.0
                    this[2, 3] = 3.0
                }
            )

        val testedMat = Matrix(3, 1).apply {
            this[0, 0] = 1.0
            this[1, 0] = 2.0
            this[2, 0] = 3.0
        }

        val tested = FrameTransformation.fromTranslation(testedMat)

        assertAll(
            { assertTrue(expected.approxEquals(tested, equalityTolerance)) },
            { assertTrue(testedMat.approxEquals(tested.translation, equalityTolerance)) }
        )
    }

    @Test
    fun `test fromRotation around z`() {
        val angle = PI / 2.0
        val cosAngle = cos(angle)
        val sinAngle = sin(angle)

        val expected =
            FrameTransformation.fromMatrix(
                Matrix.identity(4, 4).apply {
                    this[0, 0] = cosAngle
                    this[1, 0] = sinAngle
                    this[0, 1] = -sinAngle
                    this[1, 1] = cosAngle
                }
            )

        val tested = FrameTransformation.fromRotation(toDegrees(angle), 0, 0)

        assertAll(
            { assertTrue(expected.approxEquals(tested, equalityTolerance)) },
            {
                assertTrue(
                    Matrix.identity(3, 3).apply {
                        this[0, 0] = cosAngle
                        this[1, 0] = sinAngle
                        this[0, 1] = -sinAngle
                        this[1, 1] = cosAngle
                    }.approxEquals(tested.rotation, equalityTolerance)
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
            FrameTransformation.fromMatrix(
                Matrix.identity(4, 4).apply {
                    this[0, 0] = cosAngle
                    this[2, 0] = -sinAngle
                    this[0, 2] = sinAngle
                    this[2, 2] = cosAngle
                }
            )

        val tested = FrameTransformation.fromRotation(0, toDegrees(angle), 0)

        assertAll(
            { assertTrue(expected.approxEquals(tested, equalityTolerance)) },
            {
                assertTrue(
                    Matrix.identity(3, 3).apply {
                        this[0, 0] = cosAngle
                        this[2, 0] = -sinAngle
                        this[0, 2] = sinAngle
                        this[2, 2] = cosAngle
                    }.approxEquals(tested.rotation, equalityTolerance)
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
            FrameTransformation.fromMatrix(
                Matrix.identity(4, 4).apply {
                    this[1, 1] = cosAngle
                    this[2, 1] = sinAngle
                    this[1, 2] = -sinAngle
                    this[2, 2] = cosAngle
                }
            )

        val tested = FrameTransformation.fromRotation(0, 0, toDegrees(angle))

        assertAll(
            { assertTrue(expected.approxEquals(tested, equalityTolerance)) },
            {
                assertTrue(
                    Matrix.identity(3, 3).apply {
                        this[1, 1] = cosAngle
                        this[2, 1] = sinAngle
                        this[1, 2] = -sinAngle
                        this[2, 2] = cosAngle
                    }.approxEquals(tested.rotation, equalityTolerance)
                )
            }
        )
    }

    @Test
    fun `test fromRotation with Matrix`() {
        fun Matrix.populate() = apply {
            for (row in 0 until 3) {
                for (col in 0 until 3) {
                    this[row, col] = (row + col).toDouble()
                }
            }
        }

        val expected =
            FrameTransformation.fromMatrix(
                Matrix.identity(4, 4).populate()
            )
        val tested =
            FrameTransformation.fromRotation(
                Matrix(3, 3).populate()
            )

        assertAll(
            { assertEquals(expected, tested) },
            {
                assertTrue(
                    Matrix(3, 3).populate().approxEquals(
                        tested.rotation,
                        equalityTolerance
                    )
                )
            }
        )
    }

    @Test
    fun `test fromRotation with 2D double array`() {
        val data = Array(3) { row ->
            DoubleArray(3) { col -> (row * 2 + col * 3).toDouble() }
        }

        assertTrue(
            Matrix(data).approxEquals(
                FrameTransformation.fromRotation(data).rotation,
                equalityTolerance
            )
        )
    }

    @Test
    fun `test length with column vector`() {
        assertEquals(
            sqrt(1.0.pow(2) + 2.0.pow(2) + 3.0.pow(2)),
            Matrix(3, 1).apply {
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
            Matrix(1, 3).apply {
                this[0, 0] = 1.0
                this[0, 1] = 2.0
                this[0, 2] = 3.0
            }.length()
        )
    }

    @Test
    fun `test getTranslationPlanar`() {
        val expected = Matrix(2, 1).apply {
            this[0, 0] = 1.0
            this[1, 0] = 2.0
        }

        val actual = FrameTransformation.fromTranslation(1, 2, 3).translationPlanar

        assertTrue(expected.approxEquals(actual, equalityTolerance))
    }

    @Test
    fun `test getTranslationCol`() {
        val expected = Matrix(4, 1).apply {
            this[0, 0] = 1.0
            this[1, 0] = 2.0
            this[2, 0] = 3.0
            this[3, 0] = 1.0
        }

        val actual = FrameTransformation.fromTranslation(1, 2, 3).translationCol

        assertTrue(expected.approxEquals(actual, equalityTolerance))
    }

    @Test
    fun `test subMatrix`() {
        fun Matrix.populate(rowOffset: Int = 0, colOffset: Int = 0) {
            this[rowOffset + 0, colOffset + 0] = 1.0
            this[rowOffset + 0, colOffset + 1] = 2.0
            this[rowOffset + 1, colOffset + 0] = 3.0
            this[rowOffset + 1, colOffset + 1] = 4.0
        }

        val expected = Matrix(2, 2).apply {
            populate()
        }

        val actual = FrameTransformation.fromMatrix(
            Matrix(4, 4).apply {
                populate(1, 1)
            }
        ).subMatrix(1, 3, 1, 3)

        assertTrue(expected.approxEquals(actual, equalityTolerance))
    }

    @Test
    fun `test json round trip`() {
        val expected = FrameTransformation.fromTranslation(1, 2, 3)
        val klaxon = Klaxon().converter(FrameTransformation)
        val json = klaxon.toJsonString(expected)
        val ftFromJson = klaxon.parse<FrameTransformation>(json)
        assertEquals(expected, ftFromJson)
    }

    @Test
    fun `test getRotationMatrix around z only`() {
        val zRad = PI / 3

        val expected = FrameTransformation.fromRotation(Matrix.identity(3, 3).apply {
            this[0, 0] = cos(zRad)
            this[0, 1] = -sin(zRad)
            this[1, 0] = sin(zRad)
            this[1, 1] = cos(zRad)
        })

        val actual = FrameTransformation.fromRotation(toDegrees(zRad), 0, 0)

        assertTrue(expected.approxEquals(actual, equalityTolerance))
    }

    @Test
    fun `test getRotationMatrix around y only`() {
        val yRad = PI / 3

        val expected = FrameTransformation.fromRotation(
            Matrix.identity(3, 3).apply {
                this[0, 0] = cos(yRad)
                this[0, 2] = sin(yRad)
                this[2, 0] = -sin(yRad)
                this[2, 2] = cos(yRad)
            }
        )

        val actual = FrameTransformation.fromRotation(0, toDegrees(yRad), 0)

        assertTrue(expected.approxEquals(actual, equalityTolerance))
    }

    @Test
    fun `test getRotationMatrix around x only`() {
        val xRad = PI / 3

        val expected = FrameTransformation.fromRotation(
            Matrix.identity(3, 3).apply {
                this[1, 1] = cos(xRad)
                this[1, 2] = -sin(xRad)
                this[2, 1] = sin(xRad)
                this[2, 2] = cos(xRad)
            }
        )

        val actual = FrameTransformation.fromRotation(0, 0, toDegrees(xRad))

        assertTrue(expected.approxEquals(actual, equalityTolerance))
    }

    @Test
    fun `test approxEquals with negative zero`() {
        assertTrue(
            FrameTransformation.fromMatrix(
                Matrix(4, 4).apply {
                    this[0, 0] = 0.0
                }).approxEquals(
                FrameTransformation.fromMatrix(
                    Matrix(4, 4).apply {
                        this[0, 0] = -0.0
                    }),
                equalityTolerance
            )
        )
    }

    @Test
    fun `test approxEquals with tolerance`() {
        assertTrue(
            FrameTransformation.fromMatrix(
                Matrix(4, 4).apply {
                    this[0, 0] = 0.0
                }).approxEquals(
                FrameTransformation.fromMatrix(
                    Matrix(4, 4).apply {
                        this[0, 0] = equalityTolerance
                    }),
                equalityTolerance
            )
        )
    }

    @Test
    fun `test approxEquals failure`() {
        assertFalse(
            FrameTransformation.fromMatrix(
                Matrix(4, 4).apply {
                    this[0, 0] = 0.0
                }).approxEquals(
                FrameTransformation.fromMatrix(
                    Matrix(4, 4).apply {
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

    @Test
    fun `test scale with 0`() {
        assertThrows<IllegalArgumentException> {
            FrameTransformation.identity.scale(0)
        }
    }

    @Test
    fun `test scale with 1`() {
        assertThrows<IllegalArgumentException> {
            FrameTransformation.identity.scale(1)
        }
    }

    @Test
    fun `test scale with 0_5`() {
        val threshold = 1e-6
        val actual = (FrameTransformation.fromTranslation(10, 5, 2) *
            FrameTransformation.fromRotation(90, 60, 20)).scale(0.5, threshold)

        assertAll(
            { assertEquals(5.0, actual.translationX, threshold) },
            { assertEquals(2.5, actual.translationY, threshold) },
            { assertEquals(1.0, actual.translationZ, threshold) },
            { assertEquals(45.0, actual.getRotationAngles()[0], threshold) },
            { assertEquals(30.0, actual.getRotationAngles()[1], threshold) },
            { assertEquals(10.0, actual.getRotationAngles()[2], threshold) }
        )
    }

    @Test
    fun `test json`() {
        Klaxon().converter(FrameTransformation)
            .testJsonConversion(FrameTransformation.fromTranslation(10, 20, 30))
    }

    @Test
    fun `test helios json`() {
        val expected = FrameTransformation.fromTranslation(10, 20, 30)
        val json = with(FrameTransformation) { expected.encode() }
        val actual = json.decode(FrameTransformation)

        assertAll(
            { assertTrue(actual.isRight()) },
            {
                actual as Either.Right
                assertEquals(expected, actual.b)
            }
        )
    }
}
