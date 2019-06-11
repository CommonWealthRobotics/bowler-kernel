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

import org.apache.commons.math3.geometry.euclidean.threed.Rotation
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder
import org.ejml.simple.SimpleMatrix
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.immutableListOf

internal class SphericalWristTest {

    private val delta = 0.00001

    @Test
    fun `test wrist center`() {
        val wrist = SphericalWrist(
            immutableListOf(
                DhParam(1.0, 0.0, 0.0, 0.0),
                DhParam(0.0, 0.0, 1.0, -90.0),
                DhParam(1.0, 0.0, 0.0, 90.0)
            )
        )

        // target frame transformation
        val target = SimpleMatrix(4, 4)

        val rotationMatrix = Rotation(
            RotationOrder.ZYX,
            RotationConvention.FRAME_TRANSFORM,
            0.0,
            0.0,
            Math.PI * 0.5
        ).matrix

        target.setRow(0, 0, *rotationMatrix[0] + 2.0)
        target.setRow(1, 0, *rotationMatrix[1] + 0.0)
        target.setRow(2, 0, *rotationMatrix[2] + 1.0)
        target[3, 3] = 1.0

        val wristCenter = wrist.center(target)

        assertAll(
            { assertEquals(2.0, wristCenter[0, 0], delta) },
            { assertEquals(-2.0, wristCenter[1, 0], delta) },
            { assertEquals(1.0, wristCenter[2, 0], delta) }
        )
    }
}
