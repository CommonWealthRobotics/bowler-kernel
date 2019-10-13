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

import Jama.Matrix
import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.either.monad.binding
import arrow.core.fix
import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.google.common.math.DoubleMath
import helios.core.ArrayDecodingError
import helios.core.DecodingError
import helios.core.JsArray
import helios.core.JsNumber
import helios.core.JsObject
import helios.core.Json
import helios.core.KeyNotFound
import helios.core.NumberDecodingError
import helios.instances.decoder
import helios.typeclasses.Decoder
import helios.typeclasses.Encoder
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import java.util.Arrays
import kotlin.math.pow
import kotlin.math.sqrt
import org.apache.commons.math3.geometry.euclidean.threed.Rotation
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder

/**
 * An immutable frame transformation, internally back by a [Matrix].
 */
data class FrameTransformation
private constructor(private val mat: Matrix) {

    private val internalData = DoubleArray(16) {
        val row = it / 4
        val col = it % 4
        mat.array[row][col]
    }

    val data
        get() = internalData.copyOf()

    /**
     * The inverse of this [FrameTransformation].
     */
    val inverse by lazy {
        FrameTransformation(mat.inverse())
    }

    /**
     * Extracts the translation component.
     *
     * @return A 3x1 translation vector.
     */
    val translation: Matrix
        get() = mat.extractMatrix(0, 3, 3, 4)

    /**
     * Extracts the planar translation component.
     *
     * @return A 2x1 translation vector.
     */
    val translationPlanar: Matrix
        get() = mat.extractMatrix(0, 2, 3, 4)

    /**
     * Extracts the translation component plus the element from the row below it.
     *
     * @return A 4x1 translation vector.
     */
    val translationCol: Matrix
        get() = mat.extractMatrix(0, 4, 3, 4)

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
    val rotation: Matrix
        get() = mat.extractMatrix(0, 3, 0, 3)

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
    ): Matrix =
        mat.extractMatrix(rowStartInclusive, rowEndExclusive, colStartInclusive, colEndExclusive)

    operator fun plus(other: FrameTransformation) =
        FrameTransformation(mat + other.mat)

    operator fun minus(other: FrameTransformation) =
        FrameTransformation(mat - other.mat)

    operator fun times(other: FrameTransformation) =
        FrameTransformation(mat * other.mat)

    /**
     * Scales this frame transform by [t].
     *
     * @param t The scale.
     * @param threshold The convergence threshold for the iterative orthogonality correction.
     * @return A new, scaled frame transform
     */
    fun scale(t: Number, threshold: Double = 1e-6) = scale(t.toDouble(), threshold)

    /**
     * Scales this frame transform by [t].
     *
     * @param t The scale.
     * @param threshold The convergence threshold for the iterative orthogonality correction.
     * @return A new, scaled frame transform
     */
    fun scale(t: Double, threshold: Double = 1e-6): FrameTransformation {
        require(t > 0)
        require(t < 1)

        val angles = getRotationAngles(
            RotationOrder.ZYX,
            RotationConvention.VECTOR_OPERATOR,
            threshold
        )

        return fromTranslation(translation * t) * fromRotation(
            angles[0] * t,
            angles[1] * t,
            angles[2] * t,
            RotationOrder.ZYX,
            RotationConvention.VECTOR_OPERATOR
        )
    }

    /**
     * Derives the rotation angles from the [rotation] submatrix.
     *
     * @param order The rotation order.
     * @param convention The rotation convention.
     * @param threshold The convergence threshold for the iterative orthogonality correction.
     * @return The rotation angles in the [order] in degrees.
     */
    fun getRotationAngles(
        order: RotationOrder = RotationOrder.ZYX,
        convention: RotationConvention = RotationConvention.VECTOR_OPERATOR,
        threshold: Double = 1e-6
    ): List<Double> =
        Rotation(rotation.array, threshold)
            .getAngles(order, convention)
            .map { toDegrees(it) }

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

    override fun toString() = mat.array.joinToString("\n") { it.joinToString() }

    companion object : Converter, Encoder<FrameTransformation>, Decoder<FrameTransformation> {

        /**
         * The identity frame transformation.
         */
        val identity = FrameTransformation(Matrix.identity(4, 4))

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
                Matrix.identity(
                    4, 4
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
        fun fromTranslation(translation: Matrix): FrameTransformation {
            require(translation.rowDimension == 3)
            require(translation.columnDimension == 1)

            return FrameTransformation(
                Matrix.identity(4, 4).apply {
                    this[0, 3] = translation[0, 0]
                    this[1, 3] = translation[1, 0]
                    this[2, 3] = translation[2, 0]
                })
        }

        /**
         * Constructs a frame transformation from a rotation.
         *
         * @param r1 The first rotation in degrees.
         * @param r2 The second rotation in degrees.
         * @param r3 The third rotation in degrees.
         * @param order The rotation order.
         * @param convention The rotation convention.
         * @return The rotation matrix.
         */
        fun fromRotation(
            r1: Number,
            r2: Number,
            r3: Number,
            order: RotationOrder = RotationOrder.ZYX,
            convention: RotationConvention = RotationConvention.VECTOR_OPERATOR
        ) = fromRotation(
            Rotation(
                order,
                convention,
                toRadians(r1.toDouble()),
                toRadians(r2.toDouble()),
                toRadians(r3.toDouble())
            ).matrix
        )

        /**
         * Constructs a frame transformation from a rotation.
         *
         * @param rotation A 3x3 rotation matrix.
         * @return A rotation frame transformation.
         */
        fun fromRotation(rotation: Matrix) = fromRotation(rotation.array)

        /**
         * Constructs a frame transformation from a rotation.
         *
         * @param rotation A 3x3 rotation matrix.
         * @return A rotation frame transformation.
         */
        fun fromRotation(rotation: Array<DoubleArray>): FrameTransformation {
            require(rotation.size == 3)
            require(rotation[0].size == 3)

            return FrameTransformation(Matrix.identity(4, 4).apply {
                for (row in 0 until 3) {
                    for (col in 0 until 3) {
                        this[row, col] = rotation[row][col]
                    }
                }
            })
        }

        /**
         * Constructs a frame transformation from a 4x4 matrix.
         *
         * @param matrix The 4x4 matrix.
         * @return A frame transformation.
         */
        fun fromMatrix(matrix: Matrix): FrameTransformation {
            require(matrix.rowDimension == 4)
            require(matrix.columnDimension == 4)

            return FrameTransformation(matrix)
        }

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
                    Matrix(
                        Array(4) { row ->
                            DoubleArray(4) { col ->
                                data[col + row * 4]
                            }
                        }
                    )
                )
            }
        }

        override fun FrameTransformation.encode(): Json =
            JsObject(
                "rows" to JsNumber(mat.rowDimension),
                "cols" to JsNumber(mat.columnDimension),
                "data" to JsArray(internalData.map { JsNumber(it) })
            )

        fun encoder() = this

        override fun decode(value: Json): Either<DecodingError, FrameTransformation> =
            Either.applicative<DecodingError>().map(
                value["rows"].fold(
                    { Either.left(KeyNotFound("rows")) },
                    { it.decode(Int.decoder()) }),
                value["cols"].fold(
                    { Either.left(KeyNotFound("cols")) },
                    { it.decode(Int.decoder()) }),
                value["data"].fold(
                    { Either.left(KeyNotFound("data")) },
                    { it.decode(doubleArrayDecoder()) })
            ) { (rows, cols, data) ->
                require(rows == 4)
                require(cols == 4)
                require(data.size == 16)

                FrameTransformation(
                    Matrix(
                        Array(4) { row ->
                            DoubleArray(4) { col ->
                                data[col + row * 4]
                            }
                        }
                    )
                )
            }.fix()

        fun decoder() = this
    }
}

fun doubleArrayDecoder() = object : Decoder<DoubleArray> {
    override fun decode(value: Json): Either<DecodingError, DoubleArray> = binding {
        value.asJsArray()
            .toEither { ArrayDecodingError(value) }
            .bind()
            .value
            .map {
                it.asJsNumber()
                    .toEither { NumberDecodingError(it) }
                    .bind()
                    .decode(Double.decoder())
                    .bind()
            }
            .toDoubleArray()
    }
}

private fun Matrix.extractMatrix(
    startRow: Int,
    endRowExclusive: Int,
    startCol: Int,
    endColExclusive: Int
) = Matrix(endRowExclusive - startRow, endColExclusive - startCol).also {
    for (row in startRow until endRowExclusive) {
        for (col in startCol until endColExclusive) {
            it[row - startRow, col - startCol] = this[row, col]
        }
    }
}

/**
 * Treats the matrix as a row or column vector and computes its length.
 */
fun Matrix.length(): Double {
    require((rowDimension == 1 || columnDimension == 1) && rowDimension != columnDimension)

    var sum = 0.0

    if (rowDimension == 1) {
        for (i in 0 until columnDimension) {
            sum += this[0, i].pow(2)
        }
    } else {
        for (i in 0 until rowDimension) {
            sum += this[i, 0].pow(2)
        }
    }

    return sqrt(sum)
}

/**
 * Computes whether [this] is equal to [other] within a per-element [equalityTolerance].
 *
 * @param other The other matrix.
 * @param equalityTolerance The per-element tolerance.
 * @return True if [this] equals [other].
 */
fun Matrix.approxEquals(other: Matrix, equalityTolerance: Double) =
    approxEquals(other.array, equalityTolerance)

/**
 * Computes whether [this] is equal to [other] within a per-element [equalityTolerance].
 *
 * @param other The other matrix.
 * @param equalityTolerance The per-element tolerance.
 * @return True if [this] equals [other].
 */
fun Matrix.approxEquals(other: Array<DoubleArray>, equalityTolerance: Double): Boolean {
    require(rowDimension == other.size)
    require(columnDimension == other[0].size)

    for (row in 0 until rowDimension) {
        for (col in 0 until columnDimension) {
            if (!DoubleMath.fuzzyEquals(
                    this[row, col],
                    other[row][col],
                    equalityTolerance
                )
            ) {
                return false
            }
        }
    }

    return true
}

fun Matrix.joinToString(): String = array.joinToString(separator = "\n") { it.joinToString() }
