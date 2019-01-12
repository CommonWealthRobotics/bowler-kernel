/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.motion

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.neuronrobotics.kinematicschef.util.getRotationMatrix
import org.ejml.simple.SimpleMatrix
import java.util.Arrays

/**
 * An immutable frame transformation, internally back by a [SimpleMatrix].
 */
class FrameTransformation
private constructor(private val mat: SimpleMatrix) {

    private val data = DoubleArray(mat.numRows() * mat.numCols()) { mat[it] }

    /**
     * Extracts the translation component.
     *
     * @return A 3x1 translation matrix.
     */
    fun getTranslation() = mat.extractMatrix(0, 3, 3, 4)

    /**
     * Extracts the rotation component.
     *
     * @return A 3x3 rotation matrix.
     */
    fun getRotation() = mat.extractMatrix(0, 3, 0, 3)

    operator fun plus(other: FrameTransformation) = FrameTransformation(mat + other.mat)

    operator fun minus(other: FrameTransformation) = FrameTransformation(mat - other.mat)

    operator fun times(other: FrameTransformation) = FrameTransformation(mat.mult(other.mat))

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
        fun identity() = FrameTransformation(SimpleMatrix.identity(4))

        /**
         * Constructs a frame transformation from a translation.
         *
         * @param x The x-axis translation.
         * @param y The y-axis translation.
         * @param z The z-axis translation.
         * @return A translation frame transformation.
         */
        fun fromTranslation(x: Number, y: Number, z: Number) =
            FrameTransformation(SimpleMatrix.identity(4).apply {
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

            return FrameTransformation(SimpleMatrix.identity(4).apply {
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
            fromRotation(getRotationMatrix(x, y, z))

        /**
         * Constructs a frame transformation from a rotation.
         *
         * @param rotation A 3x3 rotation matrix.
         * @return A rotation frame transformation.
         */
        fun fromRotation(rotation: SimpleMatrix): FrameTransformation {
            require(rotation.numRows() == 3)
            require(rotation.numCols() == 3)

            return FrameTransformation(SimpleMatrix.identity(4).apply {
                for (row in 0 until 3) {
                    for (col in 0 until 3) {
                        this[row, col] = rotation[row, col]
                    }
                }
            })
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
                    FrameTransformation(SimpleMatrix(rows, cols, true, data))
                }
            }
        }
    }
}
