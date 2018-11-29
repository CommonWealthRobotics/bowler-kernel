/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.util

import org.ejml.simple.SimpleMatrix
import kotlin.math.sqrt

/**
 * Maps a pair of two matrices representing points into a matrix representing a line. The
 * first matrix is treated as the first point and the second matrix is treated as the second
 * point.
 */
internal fun Pair<SimpleMatrix, SimpleMatrix>.toLine(): SimpleMatrix {
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
 */
internal fun SimpleMatrix.getTranslation(): SimpleMatrix {
    require(numRows() == 4)
    require(numCols() == 4)
    return SimpleMatrix(3, 1).apply {
        this[0, 0] = this@getTranslation[0, 3]
        this[1, 0] = this@getTranslation[1, 3]
        this[2, 0] = this@getTranslation[2, 3]
    }
}

/**
 * Treats the matrix as a row or column vector and computes its length.
 */
internal fun SimpleMatrix.length(): Double {
    require(numRows() == 1 || numCols() == 1)

    var sum = 0.0

    if (numRows() == 1) {
        for (i in 0 until numCols()) {
            sum += this[0, i] * this[0, i]
        }
    } else {
        for (i in 0 until numRows()) {
            sum += this[i, 0] * this[i, 0]
        }
    }

    return sqrt(sum)
}

/**
 * Creates a 4x4 matrix representing a line.
 *
 * @param xDiff The displacement along the x-axis.
 * @param yDiff The displacement along the y-axis.
 * @param zDiff The displacement along the z-axis.
 */
internal fun getLine(xDiff: Number, yDiff: Number, zDiff: Number) =
    getPointMatrix(0.0, 0.0, 0.0) to getPointMatrix(xDiff, yDiff, zDiff)

/**
 * Creates a 4x4 matrix representing a point.
 *
 * @param x The distance along the x-axis.
 * @param y The distance along the y-axis.
 * @param z The distance along the z-axis.
 */
internal fun getPointMatrix(x: Number, y: Number, z: Number) =
    SimpleMatrix.identity(4).apply {
        this[0, 3] = x.toDouble()
        this[1, 3] = y.toDouble()
        this[2, 3] = z.toDouble()
    }
