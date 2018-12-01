/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef

import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.util.toImmutableList
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import com.nhaarman.mockitokotlin2.eq
import org.mockito.AdditionalMatchers
import kotlin.random.Random

internal object TestUtil {

    /**
     * Generate a random DH param.
     */
    internal fun randomDhParam(upperBound: Double = 90.0) = DhParam(
        Random.nextDouble(upperBound),
        Random.nextDouble(upperBound),
        Random.nextDouble(upperBound),
        Random.nextDouble(upperBound)
    )

    /**
     * Generate [listSize] number of random DH params.
     */
    internal fun randomDhParamList(listSize: Int, upperBound: Double = 90.0) =
        (0 until listSize).toList().map {
            randomDhParam(upperBound)
        }.toImmutableList()

    internal fun makeMockChain() = DHChain(object : AbstractKinematicsNR() {
        override fun forwardKinematics(p0: DoubleArray?): TransformNR {
            TODO("not implemented")
        }

        override fun disconnectDevice() {
            TODO("not implemented")
        }

        override fun inverseKinematics(p0: TransformNR?): DoubleArray {
            TODO("not implemented")
        }

        override fun connectDevice(): Boolean {
            TODO("not implemented")
        }
    })
}

/**
 * Matches any element not equal to [value].
 */
internal fun <T> not(value: T): T = AdditionalMatchers.not(eq(value))

/**
 * Matches any element equal to [first] or [second].
 */
internal fun <T> or(first: T, second: T): T = AdditionalMatchers.or(eq(first), eq(second))

/**
 * Matches any element equal to [first] and [second].
 */
internal fun <T> and(first: T, second: T): T = AdditionalMatchers.and(eq(first), eq(second))
