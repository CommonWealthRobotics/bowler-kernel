/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import com.neuronrobotics.kinematicschef.TestUtil
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.util.asPointMatrix
import com.neuronrobotics.kinematicschef.util.emptyImmutableList
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.toImmutableList
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
        testWristFails(
            DhParam(374.29, 0, 0, 90),
            DhParam(0, 72, 10, -90),
            DhParam(0, 0, 0, 90)
        )
    }

    @Test
    fun `test a non-spherical modified baxter wrist`() {
        testWristFails(
            DhParam(374.29, 0, 0, 89),
            DhParam(0, 72, 10, -90),
            DhParam(0, 0, 0, 90)
        )
    }

    @Test
    fun `test baxter's spherical wrist out of order`() {
        testWristFails(
            DhParam(0, 72, 10, -90),
            DhParam(374.29, 0, 0, 90),
            DhParam(0, 0, 0, 90)
        )
    }

    @Test
    fun `test spong spherical wrist`() {
        testWrist(
            DhParam(0, 0, 0, -90),
            DhParam(0, 0, 0, 90),
            DhParam(10, 0, 0, 0)
        )
    }

    @Test
    fun `test puma 560 spherical wrist config 1`() {
        testWrist(
            DhParam(10, 0, 10, -90),
            DhParam(0, 0, 0, 90),
            DhParam(10, 0, 0, -90)
        )
    }

    @Test
    fun `test puma 560 spherical wrist config 2`() {
        testWrist(
            DhParam(0.432, 0, 0, -90),
            DhParam(0, 0, 0, 90),
            DhParam(0.056, 0, 0, 0)
        )
    }

    @Test
    fun `test cmm input arm wrist`() {
        testWrist(
            DhParam(128, -90, 90, 90),
            DhParam(0, 0, 0, -90),
            DhParam(25, 90, 0, 0)
        )
    }

    private fun testWrist(vararg params: DhParam) {
        val chain = params.toImmutableList()
        val result = identifier.isSphericalWrist(chain)
        assertTrue(result.isEmpty())
    }

    private fun testWristFails(vararg params: DhParam) {
        val chain = params.toImmutableList()
        val result = identifier.isSphericalWrist(chain)
        assertTrue(result.nonEmpty())
    }

    private fun testWristIsFixed(vararg params: DhParam) {
        val chain = params.toImmutableList()
        val result = identifier.isSphericalWrist(
            chain,
            emptyImmutableList(),
            SphericalWrist(chain).centerHomed(emptyImmutableList()).asPointMatrix().invert()
        )
        assertTrue(result.isRight())
    }

    private fun testWristFixFails(vararg params: DhParam) {
        val chain = params.toImmutableList()
        val result = identifier.isSphericalWrist(
            chain,
            emptyImmutableList(),
            SphericalWrist(chain).centerHomed(emptyImmutableList()).asPointMatrix().invert()
        )
        assertTrue(result.isLeft())
    }
}
