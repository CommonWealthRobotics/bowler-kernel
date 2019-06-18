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
package com.neuronrobotics.kinematicschef.dhparam

import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.immutableListOf

internal class SphericalWristTest {

    private val delta = 1e-5

    @Test
    fun `test wrist center`() {
        val wrist = SphericalWrist(
            immutableListOf(
                DhParam(1.0, 0.0, 0.0, 0.0),
                DhParam(0.0, 0.0, 1.0, -90.0),
                DhParam(1.0, 0.0, 0.0, 90.0)
            )
        )

        val target = FrameTransformation.fromTranslation(2, 0, 1) *
            FrameTransformation.fromRotation(
                0,
                0,
                90,
                RotationOrder.ZYX,
                RotationConvention.FRAME_TRANSFORM
            )

        val wristCenter = wrist.center(target)

        assertAll(
            { assertEquals(2.0, wristCenter[0, 0], delta) },
            { assertEquals(-2.0, wristCenter[1, 0], delta) },
            { assertEquals(1.0, wristCenter[2, 0], delta) }
        )
    }
}
