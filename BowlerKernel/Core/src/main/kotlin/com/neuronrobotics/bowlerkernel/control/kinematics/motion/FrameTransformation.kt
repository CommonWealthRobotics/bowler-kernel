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

    fun getTranslation() = mat.extractMatrix(0, 3, 3, 4)

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

        fun identity() = FrameTransformation(SimpleMatrix.identity(4))

        fun fromTranslation(x: Number, y: Number, z: Number) =
            FrameTransformation(SimpleMatrix.identity(4).apply {
                this[0, 3] = x.toDouble()
                this[1, 3] = y.toDouble()
                this[2, 3] = z.toDouble()
            })

        fun fromRotation(x: Number, y: Number, z: Number) =
            FrameTransformation(SimpleMatrix.identity(4).apply {
                val rotMat = getRotationMatrix(x, y, z)
                for (row in 0 until rotMat.numRows()) {
                    for (col in 0 until rotMat.numCols()) {
                        this[row, col] = rotMat[row, col]
                    }
                }
            })

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
