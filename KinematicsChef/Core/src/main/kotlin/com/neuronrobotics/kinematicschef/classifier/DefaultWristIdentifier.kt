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
            val firstCoR = getPointMatrix(0.0, 0.0, 0.0) to getPointMatrix(0.0, 0.0, 1.0)

            val dhMatrices = chain
                .map {
                    it.copy(
                        theta = Math.toRadians(it.theta),
                        alpha = Math.toRadians(it.alpha)
                    )
                }
                .map {
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
            val nonzeroDotProducts = dhMatrices.map {
                val nextCoR = it.mult(currentCoR.first) to it.mult(currentCoR.second)
                val dot = nextCoR.toLine().dot(currentCoR.toLine())
                currentCoR = nextCoR
                dot
            }.filter {
                it != 0.0
            }

            /**
             * If any dot products are nonzero, then the wrist is not spherical.
             */
            return if (nonzeroDotProducts.isEmpty()) {
                Option.empty()
            } else {
                Option.just(ClassifierError("The centers of rotation are not all orthogonal."))
            }
        } else {
            return Option.just(
                ClassifierError(
                    "A chain of ${chain.size} links cannot form a spherical wrist"
                )
            )
        }
    }

    private fun Pair<SimpleMatrix, SimpleMatrix>.toLine(): SimpleMatrix {
        val out = SimpleMatrix(3, 1)

        fun writeIndex(x: Int, y: Int) {
            out[x, 0] = second[x, y] - first[x, y]
        }

        for (i in 0 until 3) {
            writeIndex(i, 3)
        }

        return out
    }

    private fun getPointMatrix(x: Double, y: Double, z: Double): SimpleMatrix {
        val out = SimpleMatrix.identity(4)

        out[0, 3] = x
        out[1, 3] = y
        out[2, 3] = z

        return out
    }
}
