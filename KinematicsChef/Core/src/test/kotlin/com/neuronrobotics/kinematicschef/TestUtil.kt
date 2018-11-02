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
import com.nhaarman.mockitokotlin2.eq
import org.mockito.AdditionalMatchers
import kotlin.random.Random

internal object TestUtil {

    /**
     * Generate a random DH param.
     */
    fun randomDhParam() = DhParam(
        Random.nextDouble(90.0),
        Random.nextDouble(90.0),
        Random.nextDouble(90.0),
        Random.nextDouble(90.0)
    )

    /**
     * Generate [listSize] number of random DH params.
     */
    fun randomDhParamList(listSize: Int) =
        (0 until listSize).toList().map {
            randomDhParam()
        }.toImmutableList()
}

/**
 * Matches any element not equal to [value].
 */
fun <T> not(value: T): T {
    return AdditionalMatchers.not(eq(value))
}
