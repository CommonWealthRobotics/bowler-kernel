/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.dhparam.toFrameTransformation
import com.neuronrobotics.kinematicschef.util.getPointMatrix
import com.neuronrobotics.kinematicschef.util.toImmutableList
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class DefaultDhClassifierTest {

    private val classifier = DefaultDhClassifier()

    @Test
    fun `test ZXZ wrist`() {
        testWrist(
            RotationOrder.ZXZ,
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
    fun `test ZYZ wrist`() {
        testWrist(
            RotationOrder.ZYZ,
            DhParam(0, 0, 0, 0),
            DhParam(0, 0, 0, -90),
            DhParam(0, 0, 0, 90)
        )
    }

    @Test
    fun `test YXZ wrist`() {
        testWrist(
            RotationOrder.YXZ,
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

    private fun testWrist(expected: RotationOrder, vararg paramArray: DhParam) {
        paramArray.toImmutableList().let { params ->
            assertTrue(
                classifier.deriveEulerAngles(
                    SphericalWrist(params),
                    getPointMatrix(10, 0, 0),
                    params.toFrameTransformation()
                ).exists { it == expected },
                "The wrist should have the expected Euler angles."
            )
        }
    }

    private fun testWristFails(vararg params: DhParam) {
        assertTrue(
            classifier.deriveEulerAngles(
                SphericalWrist(params.toImmutableList())
            ).isLeft(),
            "The wrist should not have any Euler angles."
        )
    }
}
