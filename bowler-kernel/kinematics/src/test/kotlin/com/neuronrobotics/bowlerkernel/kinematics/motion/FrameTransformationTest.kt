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
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
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
            FrameTransformation.fromMatrix(
                MatrixUtils.createRealIdentityMatrix(4)
            ),
            FrameTransformation.identity
        )
    }

    @Test
    fun `test fromTranslation with xyz and getTranslation`() {
        val expected =
            FrameTransformation.fromMatrix(
                MatrixUtils.createRealIdentityMatrix(4).apply {
                    setEntry(0, 3, 1.0)
                    setEntry(1, 3, 2.0)
                    setEntry(2, 3, 3.0)
                }
            )

        val tested = FrameTransformation.fromTranslation(1, 2, 3)

        assertAll(
            { assertEquals(expected, tested) },
            {
                assertTrue(
                    MatrixUtils.createRealMatrix(3, 1).apply {
                        setEntry(0, 0, 1.0)
                        setEntry(1, 0, 2.0)
                        setEntry(2, 0, 3.0)
                    }.isIdentical(tested.translation, equalityTolerance)
                )
            }
        )
    }

    @Test
    fun `test fromTranslation with SimpleMatrix`() {
        val expected =
            FrameTransformation.fromMatrix(
                MatrixUtils.createRealIdentityMatrix(4).apply {
                    setEntry(0, 3, 1.0)
                    setEntry(1, 3, 2.0)
                    setEntry(2, 3, 3.0)
                }
            )

        val testedMat = MatrixUtils.createRealMatrix(3, 1).apply {
            setEntry(0, 0, 1.0)
            setEntry(1, 0, 2.0)
            setEntry(2, 0, 3.0)
        }

        val tested = FrameTransformation.fromTranslation(testedMat)

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
            FrameTransformation.fromMatrix(
                MatrixUtils.createRealIdentityMatrix(4).apply {
                    setEntry(0, 0, cosAngle)
                    setEntry(1, 0, sinAngle)
                    setEntry(0, 1, -sinAngle)
                    setEntry(1, 1, cosAngle)
                }
            )

        val tested = FrameTransformation.fromRotation(getRotationMatrix(toDegrees(angle), 0, 0))

        assertAll(
            { assertTrue(expected.approxEquals(tested, equalityTolerance)) },
            {
                assertTrue(
                    MatrixUtils.createRealIdentityMatrix(3).apply {
                        setEntry(0, 0, cosAngle)
                        setEntry(1, 0, sinAngle)
                        setEntry(0, 1, -sinAngle)
                        setEntry(1, 1, cosAngle)
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
            FrameTransformation.fromMatrix(
                MatrixUtils.createRealIdentityMatrix(4).apply {
                    setEntry(0, 0, cosAngle)
                    setEntry(2, 0, -sinAngle)
                    setEntry(0, 2, sinAngle)
                    setEntry(2, 2, cosAngle)
                }
            )

        val tested = FrameTransformation.fromRotation(getRotationMatrix(0, toDegrees(angle), 0))

        assertAll(
            { assertTrue(expected.approxEquals(tested, equalityTolerance)) },
            {
                assertTrue(
                    MatrixUtils.createRealIdentityMatrix(3).apply {
                        setEntry(0, 0, cosAngle)
                        setEntry(2, 0, -sinAngle)
                        setEntry(0, 2, sinAngle)
                        setEntry(2, 2, cosAngle)
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
            FrameTransformation.fromMatrix(
                MatrixUtils.createRealIdentityMatrix(4).apply {
                    setEntry(1, 1, cosAngle)
                    setEntry(2, 1, sinAngle)
                    setEntry(1, 2, -sinAngle)
                    setEntry(2, 2, cosAngle)
                }
            )

        val tested = FrameTransformation.fromRotation(getRotationMatrix(0, 0, toDegrees(angle)))

        assertAll(
            { assertTrue(expected.approxEquals(tested, equalityTolerance)) },
            {
                assertTrue(
                    MatrixUtils.createRealIdentityMatrix(3).apply {
                        setEntry(1, 1, cosAngle)
                        setEntry(2, 1, sinAngle)
                        setEntry(1, 2, -sinAngle)
                        setEntry(2, 2, cosAngle)
                    }.isIdentical(tested.rotation, equalityTolerance)
                )
            }
        )
    }

    @Test
    fun `test fromRotation with SimpleMatrix`() {
        fun RealMatrix.populate(): RealMatrix {
            for (row in 0 until 3) {
                for (col in 0 until 3) {
                    setEntry(row, col, (row + col).toDouble())
                }
            }
            return this
        }

        val expected =
            FrameTransformation.fromMatrix(
                MatrixUtils.createRealIdentityMatrix(4).populate()
            )
        val tested =
            FrameTransformation.fromRotation(
                MatrixUtils.createRealMatrix(3, 3).populate()
            )

        assertAll(
            { assertEquals(expected, tested) },
            {
                assertTrue(
                    MatrixUtils.createRealMatrix(3, 3).populate().isIdentical(
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
            MatrixUtils.createRealMatrix(data).isIdentical(
                FrameTransformation.fromRotation(data).rotation,
                equalityTolerance
            )
        )
    }

    @Test
    fun `test length with column vector`() {
        assertEquals(
            sqrt(1.0.pow(2) + 2.0.pow(2) + 3.0.pow(2)),
            MatrixUtils.createRealMatrix(3, 1).apply {
                setEntry(0, 0, 1.0)
                setEntry(1, 0, 2.0)
                setEntry(2, 0, 3.0)
            }.length()
        )
    }

    @Test
    fun `test length with row vector`() {
        assertEquals(
            sqrt(1.0.pow(2) + 2.0.pow(2) + 3.0.pow(2)),
            MatrixUtils.createRealMatrix(1, 3).apply {
                setEntry(0, 0, 1.0)
                setEntry(0, 1, 2.0)
                setEntry(0, 2, 3.0)
            }.length()
        )
    }

    @Test
    fun `test getTranslationPlanar`() {
        val expected = MatrixUtils.createRealMatrix(2, 1).apply {
            setEntry(0, 0, 1.0)
            setEntry(1, 0, 2.0)
        }

        val actual = FrameTransformation.fromTranslation(1, 2, 3).translationPlanar

        assertTrue(expected.isIdentical(actual, equalityTolerance))
    }

    @Test
    fun `test getTranslationCol`() {
        val expected = MatrixUtils.createRealMatrix(4, 1).apply {
            setEntry(0, 0, 1.0)
            setEntry(1, 0, 2.0)
            setEntry(2, 0, 3.0)
            setEntry(3, 0, 1.0)
        }

        val actual = FrameTransformation.fromTranslation(1, 2, 3).translationCol

        assertTrue(expected.isIdentical(actual, equalityTolerance))
    }

    @Test
    fun `test subMatrix`() {
        fun RealMatrix.populate(rowOffset: Int = 0, colOffset: Int = 0): RealMatrix {
            setEntry(rowOffset + 0, colOffset + 0, 1.0)
            setEntry(rowOffset + 0, colOffset + 1, 2.0)
            setEntry(rowOffset + 1, colOffset + 0, 3.0)
            setEntry(rowOffset + 1, colOffset + 1, 4.0)
            return this
        }

        val expected = MatrixUtils.createRealMatrix(2, 2).populate()

        val actual = FrameTransformation.fromMatrix(
            MatrixUtils.createRealMatrix(4, 4).populate(1, 1)
        ).subMatrix(1, 2, 1, 2)

        assertTrue(expected.isIdentical(actual, equalityTolerance))
    }

    @Test
    fun `test json round trip`() {
        val expected = FrameTransformation.fromTranslation(1, 2, 3)
        val klaxon = Klaxon().converter(FrameTransformation.converter)
        val json = klaxon.toJsonString(expected)
        val ftFromJson = klaxon.parse<FrameTransformation>(json)
        assertEquals(expected, ftFromJson)
    }

    @Test
    fun `test getRotationMatrix around z only`() {
        val zRad = PI / 3

        val expected = MatrixUtils.createRealIdentityMatrix(3).apply {
            setEntry(0, 0, cos(zRad))
            setEntry(0, 1, -sin(zRad))
            setEntry(1, 0, sin(zRad))
            setEntry(1, 1, cos(zRad))
        }

        val actual = getRotationMatrix(toDegrees(zRad), 0, 0)

        assertTrue(expected.isIdentical(actual, equalityTolerance))
    }

    @Test
    fun `test getRotationMatrix around y only`() {
        val yRad = PI / 3

        val expected = MatrixUtils.createRealIdentityMatrix(3).apply {
            setEntry(0, 0, cos(yRad))
            setEntry(0, 2, sin(yRad))
            setEntry(2, 0, -sin(yRad))
            setEntry(2, 2, cos(yRad))
        }

        val actual = getRotationMatrix(0, toDegrees(yRad), 0)

        assertTrue(expected.isIdentical(actual, equalityTolerance))
    }

    @Test
    fun `test getRotationMatrix around x only`() {
        val xRad = PI / 3

        val expected = MatrixUtils.createRealIdentityMatrix(3).apply {
            setEntry(1, 1, cos(xRad))
            setEntry(1, 2, -sin(xRad))
            setEntry(2, 1, sin(xRad))
            setEntry(2, 2, cos(xRad))
        }

        val actual = getRotationMatrix(0, 0, toDegrees(xRad))

        assertTrue(expected.isIdentical(actual, equalityTolerance))
    }

    @Test
    fun `test approxEquals with negative zero`() {
        assertTrue(
            FrameTransformation.fromMatrix(
                MatrixUtils.createRealMatrix(4, 4).apply {
                    setEntry(0, 0, 0.0)
                }).approxEquals(
                FrameTransformation.fromMatrix(
                    MatrixUtils.createRealMatrix(4, 4).apply {
                        setEntry(0, 0, -0.0)
                    }),
                equalityTolerance
            )
        )
    }

    @Test
    fun `test approxEquals with tolerance`() {
        assertTrue(
            FrameTransformation.fromMatrix(
                MatrixUtils.createRealMatrix(4, 4).apply {
                    setEntry(0, 0, 0.0)
                }).approxEquals(
                FrameTransformation.fromMatrix(
                    MatrixUtils.createRealMatrix(4, 4).apply {
                        setEntry(0, 0, equalityTolerance)
                    }),
                equalityTolerance
            )
        )
    }

    @Test
    fun `test approxEquals failure`() {
        assertFalse(
            FrameTransformation.fromMatrix(
                MatrixUtils.createRealMatrix(4, 4).apply {
                    setEntry(0, 0, 0.0)
                }).approxEquals(
                FrameTransformation.fromMatrix(
                    MatrixUtils.createRealMatrix(4, 4).apply {
                        setEntry(0, 0, equalityTolerance + 1e-20)
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
