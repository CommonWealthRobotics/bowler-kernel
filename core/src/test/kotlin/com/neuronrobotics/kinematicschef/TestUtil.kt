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
package com.neuronrobotics.kinematicschef

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DefaultLink
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInertialStateEstimator
import com.neuronrobotics.bowlerkernel.util.Limits
import com.nhaarman.mockitokotlin2.eq
import org.mockito.AdditionalMatchers
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.toImmutableList
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

    internal val cmmInputArmDhParams = immutableListOf(
        DhParam(13, 180, 32, -90),
        DhParam(25, -90, 93, 180),
        DhParam(11, 90, 24, 90),
        DhParam(128, -90, 0, 90),
        DhParam(0, 0, 0, -90),
        DhParam(25, 90, 0, 0)
    )

    internal val hephaestusArmDhParams = immutableListOf(
        DhParam(135, 0, 0, -90),
        DhParam(0, 0, 175, 0),
        DhParam(0, 90, 169.28, 0)
    )

    internal val baxterArmDhParams = immutableListOf(
        DhParam(270.35, 0, 69, -90),
        DhParam(0, 90, 0, 90),
        DhParam(364.35, 0, 69, -90),
        DhParam(0, 0, 0, 90),
        DhParam(374.29, 0, 10, -90),
        DhParam(0, 0, 0, 90),
        DhParam(229.525, 0, 0, 0)
    )

    internal val pumaArmDhParams = immutableListOf(
        DhParam(0, 0, 0, -90),
        DhParam(14.9, 0, 43.2, 0),
        DhParam(0, 0, 2, 90),
        DhParam(43.2, 0, 0, -90),
        DhParam(0, 0, 0, 90),
        DhParam(5.6, 0, 0, 0)
    )

    internal val hephaestusArmLinks: ImmutableList<Link> = hephaestusArmDhParams.map {
        DefaultLink(
            LinkType.Rotary,
            it,
            Limits(180, -180),
            NoopInertialStateEstimator
        )
    }.toImmutableList()
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
