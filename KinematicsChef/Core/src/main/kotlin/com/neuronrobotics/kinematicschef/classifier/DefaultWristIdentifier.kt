/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import arrow.core.Option
import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.toFrameTransformation
import org.ejml.simple.SimpleMatrix

/**
 * 1. Check there are three links.
 *
 * 2. Map each link to its center of rotation. Check that a center of rotation n+1 is
 * orthogonal to the center of rotation n.
 *
 * Then the wrist is spherical.
 */
internal class DefaultWristIdentifier : WristIdentifier {

    override fun isSphericalWrist(chain: ImmutableList<DhParam>): Option<ClassifierError> {
        return if (chain.size == 3) {
            handleChainOfCorrectLength(chain)
        } else {
            Option.just(
                ClassifierError(
                    "A chain of ${chain.size} links cannot form a spherical wrist"
                )
            )
        }
    }

    private fun handleChainOfCorrectLength(chain: ImmutableList<DhParam>): Option<ClassifierError> {
        require(chain.size == 3)

        // Start with a line pointing up on z. This can be any arbitrary line since the
        // orthogonality of two lines is a relative measure.
        var currentCoR = getLine(0.0, 0.0, 1.0)

        // Map each link to its center of rotation. Check that a center of rotation n+1 is
        // orthogonal to the center of rotation n.
        val nonzeroDotProducts = chain
            .map { it.toFrameTransformation() }
            .map {
                val nextCoR = it.mult(currentCoR.first) to it.mult(currentCoR.second)
                val dot = nextCoR.toLine().dot(currentCoR.toLine())
                currentCoR = nextCoR
                dot
            }.filter { dotProduct -> dotProduct != 0.0 }

        /**
         * If any dot products are nonzero, then the wrist is not spherical.
         */
        return if (nonzeroDotProducts.isEmpty()) {
            Option.empty()
        } else {
            Option.just(ClassifierError("The centers of rotation are not all orthogonal."))
        }
    }

    /**
     * Maps a pair of two matrices representing points into a matrix representing a line. The
     * first matrix is treated as the first point and the second matrix is treated as the second
     * point.
     */
    private fun Pair<SimpleMatrix, SimpleMatrix>.toLine(): SimpleMatrix {
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
     * Creates a 4x4 matrix representing a line.
     *
     * @param xDiff The displacement along the x-axis.
     * @param yDiff The displacement along the y-axis.
     * @param zDiff The displacement along the z-axis.
     */
    private fun getLine(xDiff: Double, yDiff: Double, zDiff: Double) =
        getPointMatrix(0.0, 0.0, 0.0) to getPointMatrix(xDiff, yDiff, zDiff)

    /**
     * Creates a 4x4 matrix representing a point.
     *
     * @param x The distance along the x-axis.
     * @param y The distance along the y-axis.
     * @param z The distance along the z-axis.
     */
    private fun getPointMatrix(x: Double, y: Double, z: Double) =
        SimpleMatrix.identity(4).apply {
            this[0, 3] = x
            this[1, 3] = y
            this[2, 3] = z
        }
}
