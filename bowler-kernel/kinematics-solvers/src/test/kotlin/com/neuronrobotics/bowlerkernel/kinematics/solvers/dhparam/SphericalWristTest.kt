/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerkernel.kinematics.solvers.dhparam

import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.solvers.TestUtil.randomDhParam
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.octogonapus.ktguava.collections.immutableListOf
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
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

    @Test
    fun `make wrist with 2 params`() {
        assertThrows<IllegalArgumentException> {
            SphericalWrist(immutableListOf(randomDhParam(), randomDhParam()))
        }
    }

    @Test
    fun `make wrist with 3 params`() {
        SphericalWrist(immutableListOf(randomDhParam(), randomDhParam(), randomDhParam()))
    }

    @Test
    fun `make wrist with 4 params`() {
        assertThrows<IllegalArgumentException> {
            SphericalWrist(
                immutableListOf(
                    randomDhParam(),
                    randomDhParam(),
                    randomDhParam(),
                    randomDhParam()
                )
            )
        }
    }
}
