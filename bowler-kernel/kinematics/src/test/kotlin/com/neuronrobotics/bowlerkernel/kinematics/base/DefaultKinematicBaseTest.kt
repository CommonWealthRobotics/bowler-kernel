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

import com.neuronrobotics.bowlerkernel.kinematics.MockLimbJointsController
import com.neuronrobotics.bowlerkernel.kinematics.limb.DefaultLimb
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.kinematics.limb.LimbId
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
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.immutableMapOf
import org.octogonapus.ktguava.collections.immutableSetOf

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultKinematicBaseTest {

    private val tolerance = 1e-10

    private val limb = DefaultLimb(
        LimbId("limb"),
        seaArmLinks,
        NoopForwardKinematicsSolver,
        NoopInverseKinematicsSolver,
        LengthBasedReachabilityCalculator(),
        NoopLimbMotionPlanGenerator,
        NoopLimbMotionPlanFollower,
        MockLimbJointsController(seaArmLinks.size),
        NoopInertialStateEstimator
    )

    private fun makeBase(limbRootTransform: FrameTransformation) = DefaultKinematicBase(
        KinematicBaseId("base"),
        immutableSetOf(limb),
        immutableMapOf(limb.id to limbRootTransform)
    )

    private val motionConstraints = BasicMotionConstraints(1, 0, 0, 0)

    @Test
    fun `test setting limb tip transform with rotation on base world transform`() {
        val limbBaseFT = randomFrameTransformation()
        val base = makeBase(limbBaseFT)

        val baseCurrentFT = randomFrameTransformation()
        val desiredTipFT = randomFrameTransformation()
        base.setDesiredLimbTipTransform(
            limb.id,
            desiredTipFT,
            baseCurrentFT,
            motionConstraints
        )

        assertAll(
            {
                val expected = base.getDesiredLimbTipTransform(limb.id, baseCurrentFT)
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
    fun `test get current limb tip transform`() {
        val limbTipFT = randomFrameTransformation()
        val limb = mock<Limb> {
            on { id } doReturn LimbId("limb")
            on { getCurrentTaskSpaceTransform() } doReturn limbTipFT
        }

        val limbBaseFT = randomFrameTransformation()
        val base = DefaultKinematicBase(
            KinematicBaseId("base"),
            immutableSetOf(limb),
            immutableMapOf(limb.id to limbBaseFT)
        )

        val baseCurrentFT = randomFrameTransformation()
        val expected = limbTipFT * limbBaseFT * baseCurrentFT
        val actual = base.getCurrentLimbTipTransform(limb.id, baseCurrentFT)
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

    @Test
    fun `get world space transform in limb space`() {
        val limb = mock<Limb> {
            on { id } doReturn LimbId("limb")
        }

        val limbBaseFT = randomFrameTransformation()
        val base = DefaultKinematicBase(
            KinematicBaseId("base"),
            immutableSetOf(limb),
            immutableMapOf(limb.id to limbBaseFT)
        )

        val baseCurrentFT = randomFrameTransformation()
        val testWorldSpaceFT = randomFrameTransformation()
        val expected =
            testWorldSpaceFT * baseCurrentFT.inverse * limbBaseFT.inverse
        val actual =
            base.getWorldSpaceTransformInLimbSpace(limb.id, testWorldSpaceFT, baseCurrentFT)
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

    @Test
    fun `get limb space transform in world space`() {
        val limb = mock<Limb> {
            on { id } doReturn LimbId("limb")
        }

        val limbBaseFT = randomFrameTransformation()
        val base = DefaultKinematicBase(
            KinematicBaseId("base"),
            immutableSetOf(limb),
            immutableMapOf(limb.id to limbBaseFT)
        )

        val baseCurrentFT = randomFrameTransformation()
        val testLimbSpaceFt = randomFrameTransformation()
        val expected =
            testLimbSpaceFt * limbBaseFT * baseCurrentFT
        val actual = base.getLimbSpaceTransformInWorldSpace(limb.id, testLimbSpaceFt, baseCurrentFT)
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
