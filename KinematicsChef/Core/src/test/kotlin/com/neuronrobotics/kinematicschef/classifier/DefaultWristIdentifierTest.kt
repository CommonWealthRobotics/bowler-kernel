/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.TestUtil
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.util.asPointMatrix
import com.neuronrobotics.kinematicschef.util.emptyImmutableList
import com.neuronrobotics.kinematicschef.util.immutableListOf
import org.ejml.simple.SimpleMatrix
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals

internal class DefaultWristIdentifierTest {

    private val identifier = DefaultWristIdentifier()

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `test a chain less than three links`(chainSize: Int) {
        testWristFails(TestUtil.randomDhParamList(chainSize))
        testWristFixFails(TestUtil.randomDhParamList(chainSize))
    }

    @ParameterizedTest
    @ValueSource(ints = [4, 5, 6, 7])
    fun `test a chain more than three links`(chainSize: Int) {
        testWristFails(TestUtil.randomDhParamList(chainSize))
        testWristFixFails(TestUtil.randomDhParamList(chainSize))
    }

    @Test
    fun `test baxter's spherical wrist`() {
        testWristFails(
            immutableListOf(
                DhParam(374.29, 0, 0, 90),
                DhParam(0, 72, 10, -90),
                DhParam(0, 0, 0, 90)
            )
        )
    }

    @Test
    fun `test a non-spherical modified baxter wrist`() {
        testWristFails(
            immutableListOf(
                DhParam(374.29, 0, 0, 89),
                DhParam(0, 0, 10, -90),
                DhParam(0, 0, 0, 90)
            )
        )
    }

    @Test
    fun `test baxter's spherical wrist out of order`() {
        testWristFails(
            immutableListOf(
                DhParam(0, 0, 10, -90),
                DhParam(374.29, 0, 0, 90),
                DhParam(0, 0, 0, 90)
            )
        )
    }

    @Test
    fun `test spong spherical wrist`() {
        testWrist(
            immutableListOf(
                DhParam(0, 0, 0, -90),
                DhParam(0, 0, 0, 90),
                DhParam(10, 0, 0, 0)
            )
        )
    }

    @Test
    fun `test puma 560 spherical wrist config 1`() {
        testWrist(
            immutableListOf(
                DhParam(10, 0, 10, -90),
                DhParam(0, 0, 0, 90),
                DhParam(10, 0, 0, -90)
            )
        )
    }

    @Test
    fun `test puma 560 spherical wrist config 2`() {
        testWrist(
            immutableListOf(
                DhParam(0.432, 0, 0, -90),
                DhParam(0, 0, 0, 90),
                DhParam(0.056, 0, 0, 0)
            )
        )
    }

    @Test
    fun `test cmm input arm wrist`() {
        testWrist(
            immutableListOf(
                DhParam(128, -90, 90, 90),
                DhParam(0, 0, 0, -90),
                DhParam(25, 90, 0, 0)
            )
        )
    }

    @Test
    fun `test correct spherical wrist returns the same DH params`() {
        testWristIsFixed(
            immutableListOf(
                DhParam(0, 0, 0, -90),
                DhParam(0, 0, 0, 90),
                DhParam(10, 0, 0, 0)
            ),
            immutableListOf(
                DhParam(0, 0, 0, -90),
                DhParam(0, 0, 0, 90),
                DhParam(10, 0, 0, 0)
            )
        )
    }

    @Test
    fun `test baxter arm fix fails`() {
        testWristFixFails(
            immutableListOf(
                DhParam(374.29, 0, 0, 90),
                DhParam(0, 0, 10, -90),
                DhParam(0, 0, 0, 90)
            ),
            immutableListOf(
                DhParam(0, 0, 0, 0),
                DhParam(0, 90, 69, -90),
                DhParam(364.35, 0, 0, 90),
                DhParam(0, 0, 69, -90)
            )
        )
    }

    private fun testWrist(chain: ImmutableList<DhParam>) {
        val result = identifier.isSphericalWrist(chain)
        assertTrue(result.isEmpty())
    }

    private fun testWristFails(chain: ImmutableList<DhParam>) {
        val result = identifier.isSphericalWrist(chain)
        assertTrue(result.nonEmpty())
    }

    private fun testWristIsFixed(
        chain: ImmutableList<DhParam>,
        correctParams: ImmutableList<DhParam>,
        priorParams: ImmutableList<DhParam> = emptyImmutableList()
    ) {
        val result = identifier.isSphericalWrist(
            chain,
            priorParams,
            SphericalWrist(chain).centerHomed(priorParams).asPointMatrix().invert()
        )

        assertAll(
            { assertTrue(result.isRight()) },
            {
                assertEquals(correctParams, result.fold(
                    { emptyImmutableList<DhParam>() },
                    { it }
                ))
            }
        )
    }

    private fun testWristFixFails(
        chain: ImmutableList<DhParam>,
        priorParams: ImmutableList<DhParam> = emptyImmutableList()
    ) {
        val result = identifier.isSphericalWrist(
            chain,
            priorParams,
            if (chain.size == 3)
                SphericalWrist(chain).centerHomed(priorParams).asPointMatrix().invert()
            else
                SimpleMatrix.identity(4)
        )
        assertTrue(result.isLeft())
    }
}
