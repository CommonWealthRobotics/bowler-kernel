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
import org.ejml.simple.SimpleMatrix
import kotlin.math.cos
import kotlin.math.sin

internal class DefaultWristIdentifier : WristIdentifier {

    override fun isSphericalWrist(chain: ImmutableList<DhParam>): Option<ClassifierError> {
        /**
         * 1. Check there are three links.
         *
         * 2. Map each link to its center of rotation. Check that a center of rotation n+1 is
         * orthogonal to the center of rotation n.
         *
         * Then the wrist is spherical.
         */
        if (chain.size == 3) {
            val firstCoR = getLineVector(0.0, 0.0, 1.0)

            val dhMatrices = chain.map {
                val link1Matrix = SimpleMatrix(4, 4)
                link1Matrix[0, 0] = cos(it.theta)
                link1Matrix[1, 0] = sin(it.theta)

                link1Matrix[0, 1] = -sin(it.theta) * cos(it.alpha)
                link1Matrix[1, 1] = cos(it.theta) * cos(it.alpha)
                link1Matrix[2, 1] = sin(it.alpha)

                link1Matrix[0, 2] = sin(it.theta) * sin(it.alpha)
                link1Matrix[1, 2] = -cos(it.theta) * sin(it.alpha)
                link1Matrix[2, 2] = cos(it.alpha)

                link1Matrix[0, 3] = it.r * cos(it.theta)
                link1Matrix[1, 3] = it.r * sin(it.theta)
                link1Matrix[2, 3] = it.d
                link1Matrix[3, 3] = 1.0

                link1Matrix
            }

            var currentCoR = firstCoR
            dhMatrices.forEach {
                val nextCoR = it.mult(currentCoR)
                println("Next Center of Rotation matrix:")
                nextCoR.print()
                println("Dot product: ${nextCoR.dot(currentCoR)}\n")
                currentCoR = nextCoR
            }

            TODO("not implemented")
        } else {
            return Option.just(
                ClassifierError(
                    "A chain of ${chain.size} links cannot form a spherical wrist"
                )
            )
        }
    }

    private fun dot(a: SimpleMatrix, b: SimpleMatrix): Double {
        require(a.numRows() == 4)
        require(a.numCols() == 4)
        require(b.numRows() == 4)
        require(b.numCols() == 4)

        var sum = 0.0

        for (i in 0 until 4) {
            sum += a[i, 3] * b[i, 3]
        }

        return sum
    }

    private fun SimpleMatrix.inner(b: SimpleMatrix): Double {
        require(b.numRows() == numRows())
        require(b.numCols() == numCols())

        var sum = 0.0

        for (i in 0 until numRows()) {
            for (j in 0 until numCols()) {
                sum += this[i, j] * b[i, j]
            }
        }

        return sum
    }

    private fun getLineMatrix(x: Double, y: Double, z: Double): SimpleMatrix {
        val out = SimpleMatrix(4, 4)

        out[0, 0] = 1.0
        out[1, 1] = 1.0
        out[2, 2] = 1.0
        out[3, 3] = 1.0

        out[0, 3] = x
        out[1, 3] = y
        out[2, 3] = z

        return out
    }

    private fun getLineVector(x: Double, y: Double, z: Double): SimpleMatrix {
        val out = SimpleMatrix(4, 1)

        out[0, 0] = x
        out[1, 0] = y
        out[2, 0] = z
        out[3, 0] = 0.0

        return out
    }
}
