/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

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
import com.neuronrobotics.kinematicschef.util.emptyImmutableList
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.toImmutableList
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

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
        val result = classifier.deriveEulerAngles(
            SphericalWrist(
                immutableListOf(
                    DhParam(128, -90, 90, 90),
                    DhParam(0, 0, 0, -90),
                    DhParam(25, 90, 0, 0)
                )
            ),
            emptyImmutableList(),
            emptyImmutableList()
        )

        assertTrue(
            result.exists { it == EulerAngleXYX },
            """
                    |The wrist should have the expected Euler angles. Got:
                    |$result
                """.trimMargin()
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
}
