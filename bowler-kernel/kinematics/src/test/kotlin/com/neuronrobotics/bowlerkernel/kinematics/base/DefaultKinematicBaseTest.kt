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
@file:SuppressWarnings("LongMethod")

package com.neuronrobotics.bowlerkernel.kinematics.base

import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.SimpleKinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.BodyController
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopBodyController
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopJointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.limb.DefaultLimb
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.motion.BasicMotionConstraints
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.LengthBasedReachabilityCalculator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.kinematics.randomFrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.seaArmLinks
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktguava.collections.emptyImmutableSet
import org.octogonapus.ktguava.collections.immutableMapOf
import org.octogonapus.ktguava.collections.immutableSetOf
import org.octogonapus.ktguava.collections.toImmutableList
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultKinematicBaseTest {

    private val tolerance = 1e-10

    private val limb = DefaultLimb(
        SimpleLimbId("limb"),
        seaArmLinks,
        NoopForwardKinematicsSolver,
        NoopInverseKinematicsSolver,
        LengthBasedReachabilityCalculator(),
        NoopLimbMotionPlanGenerator,
        NoopLimbMotionPlanFollower,
        seaArmLinks.map {
            NoopJointAngleController
        }.toImmutableList(),
        NoopInertialStateEstimator
    )

    private fun makeBase(limbRootTransform: FrameTransformation) = DefaultKinematicBase(
        SimpleKinematicBaseId("base"),
        NoopBodyController,
        immutableSetOf(limb),
        immutableMapOf(limb.id to limbRootTransform)
    )

    @Test
    fun `test setting limb tip transform with rotation on base world transform`() {
        val limbBaseFT = randomFrameTransformation()
        val base = makeBase(limbBaseFT)

        val baseCurrentFT = randomFrameTransformation()
        base.setCurrentWorldSpaceTransform(baseCurrentFT)

        val desiredTipFT = randomFrameTransformation()
        base.setDesiredLimbTipTransform(
            limb.id,
            desiredTipFT,
            BasicMotionConstraints(1, 0, 0, 0)
        )

        assertAll(
            {
                val expected = base.getDesiredLimbTipTransform(limb.id)
                assertTrue(
                    expected.approxEquals(desiredTipFT, tolerance),
                    """
                    |Expected:
                    |$expected
                    |Actual:
                    |$desiredTipFT
                    """.trimMargin()
                )
            },
            {
                val expected = desiredTipFT * baseCurrentFT.inverse * limbBaseFT.inverse
                val actual = limb.getDesiredTaskSpaceTransform()
                assertTrue(
                    expected.approxEquals(actual, tolerance),
                    """
                    |Expected:
                    |$expected
                    |Actual:
                    |$actual
                    """.trimMargin()
                )
            }
        )
    }

    @Test
    fun `test get current world space transform`() {
        val baseDeltaFt = randomFrameTransformation()
        val mockBodyController = mock<BodyController> {
            on { getDeltaSinceLastDesiredTransform() } doReturn baseDeltaFt
        }

        val base = DefaultKinematicBase(
            SimpleKinematicBaseId("base"),
            mockBodyController,
            emptyImmutableSet(),
            emptyImmutableMap()
        )

        val baseCurrentFT = randomFrameTransformation()
        base.setCurrentWorldSpaceTransform(baseCurrentFT)

        assertEquals(
            baseCurrentFT * baseDeltaFt,
            base.getCurrentWorldSpaceTransformWithDelta()
        )
    }

    @Test
    fun `test get current limb tip transform`() {
        val limbCurrentFT = randomFrameTransformation()
        val limb = mock<Limb> {
            on { id } doReturn SimpleLimbId("limb")
            on { getCurrentTaskSpaceTransform() } doReturn limbCurrentFT
        }

        val baseDeltaFT = randomFrameTransformation()
        val limbBaseFT = randomFrameTransformation()
        val base = DefaultKinematicBase(
            SimpleKinematicBaseId("base"),
            mock {
                on { getDeltaSinceLastDesiredTransform() } doReturn baseDeltaFT
            },
            immutableSetOf(limb),
            immutableMapOf(limb.id to limbBaseFT)
        )

        val baseCurrentFT = randomFrameTransformation()
        base.setCurrentWorldSpaceTransform(baseCurrentFT)

        val expected = limbCurrentFT * limbBaseFT * baseCurrentFT * baseDeltaFT
        val actual = base.getCurrentLimbTipTransform(limb.id)
        assertTrue(
            expected.approxEquals(actual, tolerance),
            """
            |Expected:
            |$expected
            |Actual:
            |$actual
            """.trimMargin()
        )
    }
}
