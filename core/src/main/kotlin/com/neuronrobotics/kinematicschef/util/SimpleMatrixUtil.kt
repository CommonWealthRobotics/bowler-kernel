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
@file:SuppressWarnings("TooManyFunctions")

package com.neuronrobotics.kinematicschef.util

import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.ejml.simple.SimpleMatrix
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

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
fun getFrameTranslationMatrix(x: Number, y: Number, z: Number): SimpleMatrix =
    SimpleMatrix.identity(4).apply {
        this[0, 3] = x.toDouble()
        this[1, 3] = y.toDouble()
        this[2, 3] = z.toDouble()
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
fun identityFrameTransform(): SimpleMatrix = SimpleMatrix.identity(4)

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
 * Elementwise multiplication between the receiver matrix and [term].
 */
fun SimpleMatrix.elementMult(term: Double): SimpleMatrix {
    val termMat = SimpleMatrix(numRows(), numCols())

    for (row in 0 until termMat.numRows()) {
        for (col in 0 until termMat.numCols()) {
            termMat[row, col] = term
        }
    }

    return elementMult(termMat)
}

/**
 * Project [vectorB] onto this vector.
 */
fun SimpleMatrix.project(vectorB: SimpleMatrix): SimpleMatrix {
    require(isVector)

    val vA = vectorB - this
    val vB = negative()

    return this + vA.elementMult(vA.dot(vB) / vA.length().pow(2))
}

/**
 * Project this vector onto the plane formed by [planePoint] and [normal].
 */
fun SimpleMatrix.projectOntoPlane(planePoint: SimpleMatrix, normal: SimpleMatrix): SimpleMatrix {
    require(isVector)

    if (normal.length() == 0.0) {
        return this
    }

    val a = normal[0]
    val b = normal[1]
    val c = normal[2]
    val d = planePoint[0]
    val e = planePoint[1]
    val f = planePoint[2]
    val x = this[0]
    val y = this[1]
    val z = this[2]

    val t = (a * d - a * x + b * e - b * y + c * f - c * z) /
        (a.pow(2) + b.pow(2) + c.pow(2))

    return SimpleMatrix(3, 1).apply {
        this[0] = x + t * a
        this[1] = y + t * b
        this[2] = z + t * c
    }
}

/**
 * Cross product.
 */
fun SimpleMatrix.cross(b: SimpleMatrix): SimpleMatrix {
    require(isVector)
    require(b.isVector)

    return SimpleMatrix(3, 1).also {
        it[0] = this[1] * b[2] - this[2] * b[1]
        it[1] = this[2] * b[0] - this[0] * b[2]
        it[2] = this[0] * b[1] - this[1] * b[0]
    }
}

/**
 * Rotate all transformations to be right side up for theta456 calculations, see:
 * https://math.stackexchange.com/questions/180418/calculate-rotation-matrix-to-align-vector-a-to-vector-b-in-3d
 */
@SuppressWarnings("ReturnCount")
fun SimpleMatrix.getRotationBetween(other: SimpleMatrix): SimpleMatrix {
    val unit = divide(length())
    val b = other.divide(other.length())

    if ((unit - b).length() < 0.001) {
        return SimpleMatrix.identity(3)
    }

    val v = b.divide(b.length()).cross(unit)
    val cos = b.divide(b.length()).dot(unit) // cosine of angle

    if (cos == -1.0) {
        return SimpleMatrix.identity(3)
    }

    // skew-symmetric cross product of v
    val vx = SimpleMatrix(3, 3).also {
        it.zero()
        it[0, 1] = -v[2]
        it[0, 2] = v[1]
        it[1, 0] = v[2]
        it[1, 2] = -v[0]
        it[2, 0] = -v[1]
        it[2, 1] = v[0]
    }

    return SimpleMatrix.identity(3) + vx + vx.mult(vx).elementMult(1.0 / (1.0 + cos))
}

/**
 * Calculates the angle between the two vectors.
 */
fun SimpleMatrix.angleBetweenVector(other: SimpleMatrix): Double {
    require(isVector)
    require(other.isVector)
    return acos(dot(other) / (length() * other.length()))
}
