/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef

import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.toImmutableList
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DHLink
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration
import com.neuronrobotics.sdk.addons.kinematics.LinkFactory
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
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

    fun makeMockChain(links: Collection<DHLink>) = makeMockChain(ArrayList(links))

    internal fun makeMockChain(links: ArrayList<DHLink>) = mock<DHChain> {
        on { getLinks() } doReturn links
        on { factory } doReturn object : LinkFactory() {
            override fun getLink(c: LinkConfiguration?) = makeMockAbstractLink()

            override fun getLinkConfigurations() =
                ArrayList(links.map { makeMockAbstractLink().linkConfiguration })
        }
    }

    internal fun makeMockAbstractLink(
        upperBound: Double = 180.0,
        lowerBound: Double = -180.0
    ) = object : AbstractLink(LinkConfiguration()) {

        override fun getMaxEngineeringUnits() = upperBound

        override fun getMinEngineeringUnits() = lowerBound

        override fun getCurrentPosition(): Double {
            TODO("not implemented")
        }

        override fun cacheTargetValueDevice() {
            TODO("not implemented")
        }

        override fun flushAllDevice(p0: Double) {
            TODO("not implemented")
        }

        override fun flushDevice(p0: Double) {
            TODO("not implemented")
        }
    }

    internal fun makeFullMockChain(links: ArrayList<DHLink>) =
        DHChain(null).apply { setLinks(links) }

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
