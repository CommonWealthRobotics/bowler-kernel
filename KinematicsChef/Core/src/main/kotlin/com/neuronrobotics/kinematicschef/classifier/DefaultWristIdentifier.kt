/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import Jama.Matrix
import arrow.core.Option
import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import kotlin.math.cos
import kotlin.math.min
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
            val firstCoR = Matrix(4, 1, 0.0)
            firstCoR[2, 0] = 1.0

            val dhMatrices = chain.map {
                val link1Matrix = Matrix(4, 4, 0.0)
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
                val nextCoR = it.times(currentCoR)
                val dot = dot(nextCoR, currentCoR)
                println(dot)
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

    private fun dot(a: Matrix, b: Matrix): Double {
        var sum = 0.0

        for (i in 0 until min(a.rowDimension, b.rowDimension)) {
            sum += a[i, 0] * b[i, 0]
        }

        return sum
    }
}
