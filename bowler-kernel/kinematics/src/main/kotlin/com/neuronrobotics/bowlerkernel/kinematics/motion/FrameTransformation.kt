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

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import org.ejml.simple.SimpleMatrix
import java.util.Arrays
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * An immutable frame transformation, internally back by a [SimpleMatrix].
 */
data class FrameTransformation
private constructor(private val mat: SimpleMatrix) {

    private val data = DoubleArray(mat.numRows() * mat.numCols()) { mat[it] }

    /**
     * Extracts the translation component.
     *
     * @return A 3x1 translation vector.
     */
    fun getTranslation() = mat.extractMatrix(0, 3, 3, 4)

    /**
     * Extracts the planar translation component.
     *
     * @return A 2x1 translation vector.
     */
    fun getTranslationPlanar() = mat.extractMatrix(0, 2, 3, 4)

    /**
     * Extracts the translation component plus the element from the row below it.
     *
     * @return A 4x1 translation vector.
     */
    fun getTranslationCol() = mat.extractMatrix(0, 4, 3, 4)

    /**
     * Extracts the rotation component.
     *
     * @return A 3x3 rotation matrix.
     */
    fun getRotation() = mat.extractMatrix(0, 3, 0, 3)

    /**
     * Extracts a generic submatrix.
     *
     * @param rowStartInclusive The starting row index, inclusive.
     * @param rowEndExclusive The ending row index, exclusive.
     * @param colStartInclusive The starting column index, inclusive.
     * @param colEndExclusive The ending column index, exclusive.
     * @return A new submatrix.
     */
    fun subMatrix(
        rowStartInclusive: Int,
        rowEndExclusive: Int,
        colStartInclusive: Int,
        colEndExclusive: Int
    ) = mat.extractMatrix(rowStartInclusive, rowEndExclusive, colStartInclusive, colEndExclusive)

    operator fun plus(other: FrameTransformation) =
        FrameTransformation(mat + other.mat)

    operator fun minus(other: FrameTransformation) =
        FrameTransformation(mat - other.mat)

    operator fun times(other: FrameTransformation) =
        FrameTransformation(mat.mult(other.mat))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FrameTransformation

        return Arrays.equals(data, other.data)
    }

    override fun hashCode() = Arrays.hashCode(data)

    companion object {

        /**
         * Constructs the identity frame transformation.
         *
         * @return The identity frame transformation.
         */
        fun identity() = FrameTransformation(
            SimpleMatrix.identity(4)
        )

        /**
         * Constructs a frame transformation from a translation.
         *
         * @param x The x-axis translation.
         * @param y The y-axis translation.
         * @param z The z-axis translation.
         * @return A translation frame transformation.
         */
        fun fromTranslation(x: Number, y: Number, z: Number) =
            FrameTransformation(
                SimpleMatrix.identity(
                    4
                ).apply {
                    this[0, 3] = x.toDouble()
                    this[1, 3] = y.toDouble()
                    this[2, 3] = z.toDouble()
                })

        /**
         * Constructs a frame transformation from a translation.
         *
         * @param translation A 3x1 column vector representing the x, y, and z axis translations.
         * @return A translation frame transformation.
         */
        fun fromTranslation(translation: SimpleMatrix): FrameTransformation {
            require(translation.numRows() == 3)
            require(translation.numCols() == 1)

            return FrameTransformation(
                SimpleMatrix.identity(4).apply {
                    this[0, 3] = translation[0, 0]
                    this[1, 3] = translation[1, 0]
                    this[2, 3] = translation[2, 0]
                })
        }

        /**
         * Constructs a frame transformation from an Euler rotation. Uses ZYX rotation order.
         *
         * @param x The rotation around the x-axis.
         * @param y The rotation around the y-axis.
         * @param z The rotation around the z-axis.
         * @return A rotation frame transformation.
         */
        fun fromRotation(x: Number, y: Number, z: Number) =
            fromRotation(
                getRotationMatrix(
                    x,
                    y,
                    z
                )
            )

        /**
         * Constructs a frame transformation from a rotation.
         *
         * @param rotation A 3x3 rotation matrix.
         * @return A rotation frame transformation.
         */
        fun fromRotation(rotation: SimpleMatrix): FrameTransformation {
            require(rotation.numRows() == 3)
            require(rotation.numCols() == 3)

            return FrameTransformation(
                SimpleMatrix.identity(4).apply {
                    for (row in 0 until 3) {
                        for (col in 0 until 3) {
                            this[row, col] = rotation[row, col]
                        }
                    }
                })
        }

        fun fromSimpleMatrix(simpleMatrix: SimpleMatrix): FrameTransformation {
            require(simpleMatrix.numRows() == 4)
            require(simpleMatrix.numCols() == 4)

            return FrameTransformation(simpleMatrix)
        }

        /**
         * A Klaxon JSON converter.
         */
        val converter = object : Converter {
            override fun canConvert(cls: Class<*>) = cls == FrameTransformation::class.java

            override fun toJson(value: Any): String {
                value as FrameTransformation
                return """
                    |{
                    |   "rows": ${value.mat.numRows()},
                    |   "cols": ${value.mat.numCols()},
                    |   "data": [${value.data.joinToString(",")}]
                    |}
                """.trimMargin()
            }

            override fun fromJson(jv: JsonValue): Any? {
                return jv.obj!!.let {
                    val rows = it.int("rows")!!
                    val cols = it.int("cols")!!
                    val data = it.array<Double>("data")!!.toDoubleArray()
                    FrameTransformation(
                        SimpleMatrix(
                            rows,
                            cols,
                            true,
                            data
                        )
                    )
                }
            }
        }
    }
}

/**
 * Creates a 3x3 matrix representing a rotation. Computed using ZYX rotation order.
 *
 * @param x The rotation around the x-axis in degrees.
 * @param y The rotation around the y-axis in degrees.
 * @param z The rotation around the z-axis in degrees.
 */
fun getRotationMatrix(x: Number, y: Number, z: Number): SimpleMatrix {
    val xRad = Math.toRadians(x.toDouble())
    val yRad = Math.toRadians(y.toDouble())
    val zRad = Math.toRadians(z.toDouble())

    val zMat = SimpleMatrix.identity(3).apply {
        this[0, 0] = cos(zRad)
        this[0, 1] = -sin(zRad)
        this[1, 0] = sin(zRad)
        this[1, 1] = cos(zRad)
    }

    val yMat = SimpleMatrix.identity(3).apply {
        this[0, 0] = cos(yRad)
        this[0, 2] = sin(yRad)
        this[2, 0] = -sin(yRad)
        this[2, 2] = cos(yRad)
    }

    val xMat = SimpleMatrix.identity(3).apply {
        this[1, 1] = cos(xRad)
        this[1, 2] = -sin(xRad)
        this[2, 1] = sin(xRad)
        this[2, 2] = cos(xRad)
    }

    return zMat.mult(yMat).mult(xMat)
}

/**
 * Treats the matrix as a row or column vector and computes its length.
 */
fun SimpleMatrix.length(): Double {
    require(numRows() == 1 || numCols() == 1)

    var sum = 0.0

    if (numRows() == 1) {
        for (i in 0 until numCols()) {
            sum += this[0, i].pow(2)
        }
    } else {
        for (i in 0 until numRows()) {
            sum += this[i, 0].pow(2)
        }
    }

    return sqrt(sum)
}
