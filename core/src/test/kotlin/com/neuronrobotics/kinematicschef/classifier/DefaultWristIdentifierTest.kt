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
package com.neuronrobotics.kinematicschef.classifier

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.kinematicschef.TestUtil
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.toImmutableList

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

    @Test
    fun `test ZXZ wrist`() {
        testWrist(
            DhParam(0, 90, 0, 0),
            DhParam(0, 0, 0, 90),
            DhParam(0, -90, 0, -90)
        )
    }

    @Test
    fun `test ZXZ wrist with wrong link 1 theta`() {
        testWrist(
            DhParam(0, 91, 0, 0),
            DhParam(0, 0, 0, 90),
            DhParam(0, -90, 0, -90)
        )
    }

    @Test
    fun `test ZXZ wrist with wrong link 2 alpha`() {
        testWristFails(
            DhParam(0, 91, 0, 0),
            DhParam(0, 0, 0, 89.9),
            DhParam(0, -90, 0, -90)
        )
    }

    @Test
    fun `test ZYZ wrist`() {
        testWrist(
            DhParam(0, 0, 0, 0),
            DhParam(0, 0, 0, -90),
            DhParam(0, 0, 0, 90)
        )
    }

    @Test
    fun `test ZXY wrist`() {
        testWrist(
            DhParam(0, 90, 0, 0),
            DhParam(0, -90, 0, 90),
            DhParam(0, -90, 0, -90)
        )
    }

    @Test
    fun `test ZXY wrist with wrong thetas`() {
        testWrist(
            DhParam(0, 0, 0, 0),
            DhParam(0, 0, 0, 90),
            DhParam(0, 0, 0, -90)
        )
    }

    @Test
    fun `test ZYX wrist`() {
        testWrist(
            DhParam(0, 0, 0, 0),
            DhParam(0, 90, 0, -90),
            DhParam(0, 90, 0, 90)
        )
    }

    @Test
    fun `test YXY wrist`() {
        testWrist(
            DhParam(0, 90, 0, -90),
            DhParam(0, 0, 0, 90),
            DhParam(0, -90, 0, -90)
        )
    }

    @Test
    fun `test YZY wrist`() {
        testWrist(
            DhParam(0, 0, 0, -90),
            DhParam(0, 0, 0, 90),
            DhParam(0, 0, 0, -90)
        )
    }

    @Test
    fun `test YXZ wrist`() {
        testWrist(
            DhParam(0, 90, 0, -90),
            DhParam(0, 90, 0, 90),
            DhParam(0, -90, 0, -90)
        )
    }

    @Test
    fun `test YXZ wrist with wrong link 1 theta`() {
        testWrist(
            DhParam(0, 91, 0, -90),
            DhParam(0, 90, 0, 90),
            DhParam(0, -90, 0, -90)
        )
    }

    @Test
    fun `test YXZ wrist with wrong link 2 theta`() {
        testWrist(
            DhParam(0, 90, 0, -90),
            DhParam(0, 91, 0, 90),
            DhParam(0, -90, 0, -90)
        )
    }

    @Test
    fun `test YZX wrist`() {
        testWrist(
            DhParam(0, 0, 0, -90),
            DhParam(0, 90, 0, 90),
            DhParam(0, 0, 0, 90)
        )
    }

    @Test
    fun `test XYX wrist`() {
        testWrist(
            DhParam(0, -90, 0, 90),
            DhParam(0, 0, 0, -90),
            DhParam(0, 90, 0, 90)
        )
    }

    @Test
    fun `test XZX wrist`() {
        testWrist(
            DhParam(0, 0, 0, 90),
            DhParam(0, 0, 0, -90),
            DhParam(0, 0, 0, 90)
        )
    }

    @Test
    fun `test XYZ wrist`() {
        testWrist(
            DhParam(0, -90, 0, 90),
            DhParam(0, -90, 0, -90),
            DhParam(0, 0, 0, 90)
        )
    }

    @Test
    fun `test XZY wrist`() {
        testWrist(
            DhParam(0, 0, 0, 90),
            DhParam(0, -90, 0, -90),
            DhParam(0, 0, 0, -90)
        )
    }

    private fun testWrist(vararg chain: DhParam) = testWrist(chain.toImmutableList())

    private fun testWrist(chain: ImmutableList<DhParam>) {
        val result = identifier.isSphericalWrist(chain)
        assertTrue(result.isEmpty())
    }

    private fun testWristFails(vararg chain: DhParam) = testWristFails(chain.toImmutableList())

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
            FrameTransformation.fromTranslation(
                SphericalWrist(chain).centerHomed(priorParams)
            ).inverse
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
//            if (chain.size == 3)
//                FrameTransformation.fromTranslation(
//                    SphericalWrist(chain).centerHomed(priorParams)
//                ).inverse
//            else
            FrameTransformation.identity
        )
        assertTrue(result.isLeft())
    }
}
