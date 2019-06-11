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
import com.neuronrobotics.kinematicschef.TestUtil
import com.neuronrobotics.kinematicschef.dhparam.DhChainElement
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngle
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleXYX
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleXYZ
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleXZX
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleXZY
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleYXY
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleYXZ
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleYZX
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleYZY
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleZXY
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleZXZ
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleZYX
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngleZYZ
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.toImmutableList

class DefaultDhClassifierTest {

    private val classifier = DefaultDhClassifier()

    @Test
    fun `test ZXZ wrist`() {
        testWrist(
            EulerAngleZXZ,
            DhParam(0, 90, 0, 0),
            DhParam(0, 0, 0, 90),
            DhParam(0, -90, 0, -90)
        )
    }

    @Test
    fun `test ZXZ wrist with wrong link 1 theta`() {
        testWristFails(
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
            EulerAngleZYZ,
            DhParam(0, 0, 0, 0),
            DhParam(0, 0, 0, -90),
            DhParam(0, 0, 0, 90)
        )
    }

    @Test
    fun `test ZXY wrist`() {
        testWrist(
            EulerAngleZXY,
            DhParam(0, 90, 0, 0),
            DhParam(0, -90, 0, 90),
            DhParam(0, -90, 0, -90)
        )
    }

    @Test
    fun `test ZXY wrist with wrong thetas`() {
        testWristFails(
            DhParam(0, 0, 0, 0),
            DhParam(0, 0, 0, 90),
            DhParam(0, 0, 0, -90)
        )
    }

    @Test
    fun `test ZYX wrist`() {
        testWrist(
            EulerAngleZYX,
            DhParam(0, 0, 0, 0),
            DhParam(0, 90, 0, -90),
            DhParam(0, 90, 0, 90)
        )
    }

    @Test
    fun `test YXY wrist`() {
        testWrist(
            EulerAngleYXY,
            DhParam(0, 90, 0, -90),
            DhParam(0, 0, 0, 90),
            DhParam(0, -90, 0, -90)
        )
    }

    @Test
    fun `test YZY wrist`() {
        testWrist(
            EulerAngleYZY,
            DhParam(0, 0, 0, -90),
            DhParam(0, 0, 0, 90),
            DhParam(0, 0, 0, -90)
        )
    }

    @Test
    fun `test YXZ wrist`() {
        testWrist(
            EulerAngleYXZ,
            DhParam(0, 90, 0, -90),
            DhParam(0, 90, 0, 90),
            DhParam(0, -90, 0, -90)
        )
    }

    @Test
    fun `test YXZ wrist with wrong link 1 theta`() {
        testWristFails(
            DhParam(0, 91, 0, -90),
            DhParam(0, 90, 0, 90),
            DhParam(0, -90, 0, -90)
        )
    }

    @Test
    fun `test YXZ wrist with wrong link 2 theta`() {
        testWristFails(
            DhParam(0, 90, 0, -90),
            DhParam(0, 91, 0, 90),
            DhParam(0, -90, 0, -90)
        )
    }

    @Test
    fun `test YZX wrist`() {
        testWrist(
            EulerAngleYZX,
            DhParam(0, 0, 0, -90),
            DhParam(0, 90, 0, 90),
            DhParam(0, 0, 0, 90)
        )
    }

    @Test
    fun `test XYX wrist`() {
        testWrist(
            EulerAngleXYX,
            DhParam(0, -90, 0, 90),
            DhParam(0, 0, 0, -90),
            DhParam(0, 90, 0, 90)
        )
    }

    @Test
    fun `test XZX wrist`() {
        testWrist(
            EulerAngleXZX,
            DhParam(0, 0, 0, 90),
            DhParam(0, 0, 0, -90),
            DhParam(0, 0, 0, 90)
        )
    }

    @Test
    fun `test XYZ wrist`() {
        testWrist(
            EulerAngleXYZ,
            DhParam(0, -90, 0, 90),
            DhParam(0, -90, 0, -90),
            DhParam(0, 0, 0, 90)
        )
    }

    @Test
    fun `test XZY wrist`() {
        testWrist(
            EulerAngleXZY,
            DhParam(0, 0, 0, 90),
            DhParam(0, -90, 0, -90),
            DhParam(0, 0, 0, -90)
        )
    }

    @Test
    fun `test cmm input arm wrist`() {
        testWristIsFixable(
            EulerAngleXYX,
            TestUtil.cmmInputArmDhParams.subList(3, 6)
        )
    }

    @Test
    fun `test puma arm wrist`() {
        testWristIsFixable(
            EulerAngleYZY,
            TestUtil.pumaArmDhParams.subList(3, 6)
        )
    }

    private fun testWrist(expected: EulerAngle, vararg paramArray: DhParam) {
        paramArray.toImmutableList().let { params ->
            val result = classifier.deriveEulerAngles(
                SphericalWrist(params)
            )

            assertTrue(
                result.exists { it == expected },
                """
                |The wrist should have the expected Euler angles. Got:
                |$result
                """.trimMargin()
            )
        }
    }

    private fun testWristFails(vararg params: DhParam) {
        val result = classifier.deriveEulerAngles(
            SphericalWrist(params.toImmutableList())
        )

        assertTrue(
            result.isLeft(),
            """
            |The wrist should not have any Euler angles. Got:
            |$result
            """.trimMargin()
        )
    }

    private fun testWristIsFixable(
        expected: EulerAngle,
        params: ImmutableList<DhParam>,
        priorLinks: ImmutableList<DhChainElement> = emptyImmutableList(),
        followingLinks: ImmutableList<DhChainElement> = emptyImmutableList()
    ) {
        val result = classifier.deriveEulerAngles(
            SphericalWrist(params),
            priorLinks,
            followingLinks
        )

        assertTrue(
            result.exists { it == expected },
            """
            |The wrist should have the expected Euler angles. Got:
            |$result
            """.trimMargin()
        )
    }
}
