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
package com.neuronrobotics.bowlerkernel.kinematics.motion

import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DefaultLink
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.util.Limits
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.octogonapus.ktguava.collections.immutableListOf

internal class LengthBasedReachabilityCalculatorTest {

    private val calculator = LengthBasedReachabilityCalculator()

    @Test
    fun `test easily reachable target`() {
        val links = immutableListOf(
            DefaultLink(
                LinkType.Rotary,
                DhParam(0, 0, 15, 0),
                Limits(180.0, -180.0),
                NoopInertialStateEstimator
            ) as Link
        )

        // This is technically unreachable because the limb is one 15-unit-long link, but this
        // solver isn't meant to catch this case
        val target = FrameTransformation.fromTranslation(10, 0, 0)

        assertTrue(calculator.isFrameTransformationReachable(target, links))
    }

    @Test
    fun `test target the length of the limb`() {
        val links = immutableListOf(
            DefaultLink(
                LinkType.Rotary,
                DhParam(0, 0, 10, 0),
                Limits(180.0, -180.0),
                NoopInertialStateEstimator
            ) as Link
        )

        val target = FrameTransformation.fromTranslation(10, 0, 0)

        assertTrue(calculator.isFrameTransformationReachable(target, links))
    }

    @Test
    fun `test target longer than the limb`() {
        val links = immutableListOf(
            DefaultLink(
                LinkType.Rotary,
                DhParam(0, 0, 10, 0),
                Limits(180.0, -180.0),
                NoopInertialStateEstimator
            ) as Link
        )

        val target = FrameTransformation.fromTranslation(15, 0, 0)

        assertFalse(calculator.isFrameTransformationReachable(target, links))
    }
}
