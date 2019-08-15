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
package com.neuronrobotics.bowlerkernel.kinematics.limb

import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.motion.BasicMotionConstraints
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.LengthBasedReachabilityCalculator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanFollower
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.octogonapus.ktguava.collections.emptyImmutableList
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultLimbTest {

    @Test
    fun `test setDesiredTaskSpaceTransform always finishes moving`() {
        val limb = DefaultLimb(
            SimpleLimbId("A"),
            emptyImmutableList(),
            NoopForwardKinematicsSolver,
            NoopInverseKinematicsSolver,
            LengthBasedReachabilityCalculator(),
            mock {
                on {
                    generatePlanForTaskSpaceTransform(any(), any(), any(), any())
                } doThrow RuntimeException("Boom!")
            },
            NoopLimbMotionPlanFollower,
            emptyImmutableList(),
            NoopInertialStateEstimator
        )

        limb.setDesiredTaskSpaceTransform(
            FrameTransformation.fromTranslation(10, 0, 0),
            BasicMotionConstraints(1000, 10, 100, 100)
        )

        while (limb.isMovingToTaskSpaceTransform()) {
            Thread.sleep(10)
        }

        assertFalse(limb.isMovingToTaskSpaceTransform())
    }
}
