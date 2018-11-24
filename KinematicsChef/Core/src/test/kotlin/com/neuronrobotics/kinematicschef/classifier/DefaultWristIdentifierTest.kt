/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import com.neuronrobotics.kinematicschef.TestUtil
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.util.immutableListOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class DefaultWristIdentifierTest {

    private val identifier = DefaultWristIdentifier()

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `test a chain less than three links`(chainSize: Int) {
        assertTrue(
            identifier.isSphericalWrist(
                TestUtil.randomDhParamList(chainSize)
            ).nonEmpty()
        )
    }

    @ParameterizedTest
    @ValueSource(ints = [4, 5, 6, 7])
    fun `test a chain more than three links`(chainSize: Int) {
        assertTrue(
            identifier.isSphericalWrist(
                TestUtil.randomDhParamList(chainSize)
            ).nonEmpty()
        )
    }

    @Test
    fun `test baxter's spherical wrist`() {
        val baxterLink5 = DhParam(374.29, 0, 0, 90)
        val baxterLink6 = DhParam(0, 72, 10, -90)
        val baxterLink7 = DhParam(0, 0, 0, 90)

        val chain = immutableListOf(baxterLink5, baxterLink6, baxterLink7)
        val result = identifier.isSphericalWrist(chain)
        assertTrue(result.nonEmpty())
    }

    @Test
    fun `test a non-spherical modified baxter wrist`() {
        val baxterLink5 = DhParam(374.29, 0, 0, 89)
        val baxterLink6 = DhParam(0, 72, 10, -90)
        val baxterLink7 = DhParam(0, 0, 0, 90)

        val chain = immutableListOf(baxterLink5, baxterLink6, baxterLink7)
        val result = identifier.isSphericalWrist(chain)
        assertTrue(result.nonEmpty())
    }

    @Test
    fun `test baxter's spherical wrist out of order`() {
        val baxterLink5 = DhParam(374.29, 0, 0, 90)
        val baxterLink6 = DhParam(0, 72, 10, -90)
        val baxterLink7 = DhParam(0, 0, 0, 90)

        val chain = immutableListOf(baxterLink6, baxterLink5, baxterLink7)
        val result = identifier.isSphericalWrist(chain)
        assertTrue(result.nonEmpty())
    }

    @Test
    fun `test spong spherical wrist`() {
        val link1 = DhParam(0, 0, 0, -90)
        val link2 = DhParam(0, 0, 0, 90)
        val link3 = DhParam(10, 0, 0, 0)

        val chain = immutableListOf(link1, link2, link3)
        val result = identifier.isSphericalWrist(chain)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test puma 560 spherical wrist config 1`() {
        val link4 = DhParam(10, 0, 10, -90)
        val link5 = DhParam(0, 0, 0, 90)
        val link6 = DhParam(0, 0, 0, -90)

        val chain = immutableListOf(link4, link5, link6)
        val result = identifier.isSphericalWrist(chain)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test puma 560 spherical wrist config 2`() {
        val link4 = DhParam(0.432, 0, 0, -90)
        val link5 = DhParam(0, 0, 0, 90)
        val link6 = DhParam(0.056, 0, 0, 0)

        val chain = immutableListOf(link4, link5, link6)
        val result = identifier.isSphericalWrist(chain)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test cmm input arm wrist`() {
        val link4 = DhParam(128, -90, 90, 90)
        val link5 = DhParam(0, 0, 0, -90)
        val link6 = DhParam(25, 90, 0, 0)

        val chain = immutableListOf(link4, link5, link6)
        val result = identifier.isSphericalWrist(chain)
        assertTrue(result.isEmpty())
    }
}
