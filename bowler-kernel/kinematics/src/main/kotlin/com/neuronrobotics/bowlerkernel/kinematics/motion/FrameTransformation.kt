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
@file:SuppressWarnings("TooManyFunctions", "LargeClass")

package com.neuronrobotics.bowlerkernel.kinematics.motion

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.google.common.math.DoubleMath
import org.apache.commons.math3.geometry.euclidean.threed.Rotation
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.ejml.simple.SimpleMatrix
import java.lang.Math.toRadians
import java.util.Arrays
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * An immutable frame transformation, internally back by a [SimpleMatrix].
 */
data class FrameTransformation
private constructor(private val mat: RealMatrix) {

    init {
        require(mat.rowDimension == 4) {
            "The input matrix must have 4 rows, given ${mat.rowDimension}"
        }

        require(mat.columnDimension == 4) {
            "The input matrix must have 4 columns, given ${mat.columnDimension}"
        }
    }

    // The let is not redundant because mat.data makes a copy
    @Suppress("ReplaceSingleLineLet")
    private val internalData = mat.data.let { data ->
        DoubleArray(16) {
            val row = it / 4
            val col = it % 4
            data[row][col]
        }
    }

    val data
        get() = internalData.copyOf()

    /**
     * Extracts the translation component.
     *
     * @return A 3x1 translation vector.
     */
    val translation: RealMatrix
        get() = mat.getSubMatrix(0, 2, 3, 3)

    /**
     * Extracts the planar translation component.
     *
     * @return A 2x1 translation vector.
     */
    val translationPlanar: RealMatrix
        get() = mat.getSubMatrix(0, 1, 3, 3)

    /**
     * Extracts the translation component plus the element from the row below it.
     *
     * @return A 4x1 translation vector.
     */
    val translationCol: RealMatrix
        get() = mat.getSubMatrix(0, 3, 3, 3)

    /**
     * Extracts the X component of translation.
     */
    val translationX: Double = internalData[3]

    /**
     * Extracts the Y component of translation.
     */
    val translationY: Double = internalData[7]

    /**
     * Extracts the Z component of translation.
     */
    val translationZ: Double = internalData[11]

    /**
     * Extracts the rotation component.
     *
     * @return A 3x3 rotation matrix.
     */
    val rotation: RealMatrix
        get() = mat.getSubMatrix(0, 2, 0, 2)

    /**
     * Extracts a generic submatrix.
     *
     * @param rowStartInclusive The starting row index, inclusive.
     * @param rowEndInclusive The ending row index, inclusive.
     * @param colStartInclusive The starting column index, inclusive.
     * @param colEndInclusive The ending column index, inclusive.
     * @return A new submatrix.
     */
    fun subMatrix(
        rowStartInclusive: Int,
        rowEndInclusive: Int,
        colStartInclusive: Int,
        colEndInclusive: Int
    ): RealMatrix =
        mat.getSubMatrix(rowStartInclusive, rowEndInclusive, colStartInclusive, colEndInclusive)

    operator fun plus(other: FrameTransformation) =
        FrameTransformation(mat.add(other.mat))

    operator fun minus(other: FrameTransformation) =
        FrameTransformation(mat.subtract(other.mat))

    operator fun times(other: FrameTransformation) =
        FrameTransformation(mat.multiply(other.mat))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FrameTransformation

        return Arrays.equals(internalData, other.internalData)
    }

    override fun hashCode() = Arrays.hashCode(internalData)

    /**
     * Computes whether this [FrameTransformation] is approximate equal to the [other]
     * [FrameTransformation] within the [tolerance].
     *
     * @param other The other [FrameTransformation].
     * @param tolerance The per-element tolerance.
     * @return True if this is equal to [other].
     */
    fun approxEquals(other: FrameTransformation, tolerance: Double): Boolean {
        internalData.forEachIndexed { index, elem ->
            if (!DoubleMath.fuzzyEquals(elem, other.internalData[index], tolerance)) {
                return false
            }
        }

        return true
    }

    companion object {

        /**
         * The identity frame transformation.
         */
        val identity = FrameTransformation(MatrixUtils.createRealIdentityMatrix(4))

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
                MatrixUtils.createRealIdentityMatrix(4).apply {
                    setEntry(0, 3, x.toDouble())
                    setEntry(1, 3, y.toDouble())
                    setEntry(2, 3, z.toDouble())
                })

        /**
         * Constructs a frame transformation from a translation.
         *
         * @param translation A 3x1 column vector representing the x, y, and z axis translations.
         * @return A translation frame transformation.
         */
        fun fromTranslation(translation: RealMatrix): FrameTransformation {
            require(translation.rowDimension == 3)
            require(translation.columnDimension == 1)

            return FrameTransformation(
                MatrixUtils.createRealIdentityMatrix(4).apply {
                    setEntry(0, 3, translation.getEntry(0, 0))
                    setEntry(1, 3, translation.getEntry(1, 0))
                    setEntry(2, 3, translation.getEntry(2, 0))
                })
        }

        /**
         * Constructs a frame transformation from a rotation.
         *
         * @param rotation A 3x3 rotation matrix.
         * @return A rotation frame transformation.
         */
        fun fromRotation(rotation: RealMatrix): FrameTransformation {
            require(rotation.rowDimension == 3)
            require(rotation.columnDimension == 3)

            return FrameTransformation(MatrixUtils.createRealIdentityMatrix(4).apply {
                for (row in 0 until 3) {
                    for (col in 0 until 3) {
                        setEntry(row, col, rotation.getEntry(row, col))
                    }
                }
            })
        }

        /**
         * Constructs a frame transformation from a rotation.
         *
         * @param rotation A 3x3 rotation matrix.
         * @return A rotation frame transformation.
         */
        fun fromRotation(rotation: Array<DoubleArray>): FrameTransformation {
            require(rotation.size == 3)
            require(rotation[0].size == 3)

            return FrameTransformation(MatrixUtils.createRealIdentityMatrix(4).apply {
                for (row in 0 until 3) {
                    for (col in 0 until 3) {
                        setEntry(row, col, rotation[row][col])
                    }
                }
            })
        }

        fun fromMatrix(matrix: RealMatrix): FrameTransformation {
            require(matrix.rowDimension == 4)
            require(matrix.columnDimension == 4)

            return FrameTransformation(matrix)
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
                    |   "rows": ${value.mat.rowDimension},
                    |   "cols": ${value.mat.columnDimension},
                    |   "data": [${value.internalData.joinToString(",")}]
                    |}
                """.trimMargin()
            }

            override fun fromJson(jv: JsonValue): Any? {
                return jv.obj!!.let {
                    val rows = it.int("rows")!!
                    val cols = it.int("cols")!!
                    val data = it.array<Double>("data")!!.toDoubleArray()

                    require(rows == 4)
                    require(cols == 4)
                    require(data.size == 16)

                    FrameTransformation(
                        MatrixUtils.createRealMatrix(
                            Array(4) { row ->
                                DoubleArray(4) { col ->
                                    data[col + row * 4]
                                }
                            }
                        )
                    )
                }
            }
        }
    }
}

/**
 * Creates a 3x3 matrix representing a rotation.
 *
 * @param r1 The first rotation in degrees.
 * @param r2 The second rotation in degrees.
 * @param r3 The third rotation in degrees.
 * @param order The rotation order.
 * @param convention The rotation convention.
 * @return The rotation matrix.
 */
fun getRotationMatrix(
    r1: Number,
    r2: Number,
    r3: Number,
    order: RotationOrder = RotationOrder.ZYX,
    convention: RotationConvention = RotationConvention.VECTOR_OPERATOR
): RealMatrix = Rotation(
    order,
    convention,
    toRadians(r1.toDouble()),
    toRadians(r2.toDouble()),
    toRadians(r3.toDouble())
).let { MatrixUtils.createRealMatrix(it.matrix) }

/**
 * Treats the matrix as a row or column vector and computes its length.
 *
 * @return The length.
 */
fun RealMatrix.length(): Double {
    require((rowDimension == 1 || columnDimension == 1) && !isSquare)

    return sqrt(
        if (rowDimension == 1) {
            getRow(0).sumByDouble { it.pow(2) }
        } else {
            getColumn(0).sumByDouble { it.pow(2) }
        }
    )
}

/**
 * Computes whether [this] is equal to [other] within a per-element [equalityTolerance].
 *
 * @param other The other matrix.
 * @param equalityTolerance The per-element tolerance.
 * @return True if [this] equals [other].
 */
fun RealMatrix.isIdentical(other: RealMatrix, equalityTolerance: Double): Boolean {
    require(rowDimension == other.rowDimension)
    require(columnDimension == other.columnDimension)

    for (row in 0 until rowDimension) {
        for (col in 0 until columnDimension) {
            if (!DoubleMath.fuzzyEquals(
                    getEntry(row, col),
                    other.getEntry(row, col),
                    equalityTolerance
                )
            ) {
                return false
            }
        }
    }

    return true
}
