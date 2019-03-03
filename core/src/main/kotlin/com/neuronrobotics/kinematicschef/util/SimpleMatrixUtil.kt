/*
 * This file is part of kinematics-chef.
 *
 * kinematics-chef is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kinematics-chef is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with kinematics-chef.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.kinematicschef.util

import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.ejml.simple.SimpleMatrix
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Maps a pair of two matrices representing points into a matrix representing a line. The
 * first matrix is treated as the first point and the second matrix is treated as the second
 * point.
 */
fun Pair<SimpleMatrix, SimpleMatrix>.toLine(): SimpleMatrix {
    require(first.numRows() == 4)
    require(first.numCols() == 4)
    require(second.numRows() == 4)
    require(second.numCols() == 4)

    val out = SimpleMatrix(3, 1)

    fun writeDiffToElement(x: Int, y: Int) {
        out[x, 0] = second[x, y] - first[x, y]
    }

    for (i in 0 until 3) {
        writeDiffToElement(i, 3)
    }

    return out
}

/**
 * Extracts the translation component from a 4x4 frame transformation.
 *
 * @return A 3x1 translation matrix.
 */
fun SimpleMatrix.getTranslation(): SimpleMatrix {
    require(numRows() == 4)
    require(numCols() == 4)
    return extractMatrix(0, 3, 3, 4)
}

/**
 * Extracts the rotation component from a 4x4 frame transformation.
 *
 * @return a 3x3 rotation matrix.
 */
fun SimpleMatrix.getRotation(): SimpleMatrix {
    require(numRows() == 4)
    require(numCols() == 4)
    return extractMatrix(0, 3, 0, 3)
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

/**
 * Creates a 4x4 frame transformation representing a translation.
 *
 * @param x The distance along the x-axis.
 * @param y The distance along the y-axis.
 * @param z The distance along the z-axis.
 */
fun getFrameTranslationMatrix(x: Number, y: Number, z: Number) =
    SimpleMatrix.identity(4).apply {
        this[0, 3] = x.toDouble()
        this[1, 3] = y.toDouble()
        this[2, 3] = z.toDouble()
    }

/**
 * Creates a 4x4 frame transformation representing a rotation.
 *
 * @param x The rotation around the x-axis in degrees.
 * @param y The rotation around the y-axis in degrees.
 * @param z The rotation around the z-axis in degrees.
 */
fun getFrameRotationMatrix(x: Number, y: Number, z: Number) =
    SimpleMatrix.identity(4).apply {
        val rotMat = getRotationMatrix(x, y, z)
        for (row in 0 until rotMat.numRows()) {
            for (col in 0 until rotMat.numCols()) {
                this[row, col] = rotMat[row, col]
            }
        }
    }

/**
 * Creates a 3x3 matrix representing a rotation.
 *
 * @param x The rotation around the x-axis in degrees.
 * @param y The rotation around the y-axis in degrees.
 * @param z The rotation around the z-axis in degrees.
 */
fun getRotationMatrix(x: Number, y: Number, z: Number): SimpleMatrix {
    val xRad = toRadians(x.toDouble())
    val yRad = toRadians(y.toDouble())
    val zRad = toRadians(z.toDouble())

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
 * Treats the receiver matrix as a 3x1 translation column vector and returns it as a 4x4 frame
 * transformation.
 */
fun SimpleMatrix.toTranslation(): SimpleMatrix {
    require(numRows() == 3)
    require(numCols() == 1)
    return getFrameTranslationMatrix(this[0, 0], this[1, 0], this[2, 0])
}

/**
 * Returns a frame transformation which applies no transformation.
 */
fun identityFrameTransform() = SimpleMatrix.identity(4)

/**
 * Returns a [SimpleMatrix] which is equivalent to the [TransformNR].
 */
fun TransformNR.toSimpleMatrix(): SimpleMatrix {
    val out = SimpleMatrix(4, 4)

    val mat = matrixTransform
    for (row in 0 until mat.rowDimension) {
        for (col in 0 until mat.columnDimension) {
            out[row, col] = mat[row, col]
        }
    }

    return out
}

/**
 * Returns a [TransformNR] which is equivalent to the [SimpleMatrix].
 */
fun SimpleMatrix.toTransformNR(): TransformNR {
    val translationMat = getTranslation()
    val rotationMat = getRotation()

    val translation = DoubleArray(3) { translationMat[it] }
    val rotation = Array(3) { row ->
        DoubleArray(3) { col ->
            rotationMat[row, col]
        }
    }

    return TransformNR(translation, rotation)
}

/**
 * Projects the receiver vector onto the plane specified by [planeNormal].
 */
fun SimpleMatrix.projectionOntoPlane(planeNormal: SimpleMatrix): SimpleMatrix {
    require(numRows() == 3)
    require(numCols() == 1)
    require(planeNormal.numRows() == 3)
    require(planeNormal.numCols() == 1)
    return minus(planeNormal.elementMult(dot(planeNormal.normalize())))
}

/**
 * Projects the receiver vector onto [unitVector].
 */
fun SimpleMatrix.projectionOntoVector(unitVector: SimpleMatrix): Double {
    require(numRows() == 2)
    require(numCols() == 1)
    require(unitVector.numRows() == 2)
    require(unitVector.numCols() == 1)
    return dot(unitVector.normalize())
}

/**
 * Returns a normalized version of the receiver matrix.
 */
private fun SimpleMatrix.normalize() = divide(length())

/**
 * Elementwise multiplication between the receiver matrix and [term].
 */
fun SimpleMatrix.elementMult(term: Double): SimpleMatrix {
    val termMat = SimpleMatrix(numRows(), numCols()).apply {
        for (row in 0 until numRows()) {
            for (col in 0 until numCols()) {
                this[row, col] = term
            }
        }
    }

    return elementMult(termMat)
}
