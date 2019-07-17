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
package com.neuronrobotics.bowlerkernel.kinematics.base

import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopJointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.limb.DefaultLimb
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.motion.LengthBasedReachabilityCalculator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.kinematics.seaArmLinks
import org.junit.jupiter.api.Test
import org.octogonapus.ktguava.collections.toImmutableList

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

    // private fun makeBase(limbRootTransform: FrameTransformation) = DefaultKinematicBase(
    //     SimpleKinematicBaseId("base"),
    //     immutableListOf(limb),
    //     immutableMapOf(limb.id to limbRootTransform),
    //     NoopBodyController
    // )

    @Test
    fun `test setting limb tip transform`() {
        TODO()
        // val base = makeBase(FrameTransformation.fromTranslation(10, 0, -10))
        //
        // base.setCurrentWorldSpaceTransform(
        //     FrameTransformation.fromTranslation(10, 10, 0)
        // )
        //
        // val desiredTipTransform = FrameTransformation.fromTranslation(25, 0, -15)
        // base.setDesiredLimbTipTransform(
        //     limb.id,
        //     desiredTipTransform,
        //     BasicMotionConstraints(1, 0, 0, 0)
        // )
        //
        // assertAll(
        //     {
        //         val expected = base.getDesiredLimbTipTransform(limb.id)
        //         assertTrue(
        //             expected.approxEquals(desiredTipTransform, tolerance),
        //             """
        //             |Expected:
        //             |$expected
        //             |Actual:
        //             |$desiredTipTransform
        //             """.trimMargin()
        //         )
        //     },
        //     {
        //         val expected = FrameTransformation.fromTranslation(
        //             x = 25 - 10 - 10,
        //             y = 0 - 10,
        //             z = -15 - -10
        //         )
        //         val actual = limb.getDesiredTaskSpaceTransform()
        //         assertTrue(
        //             expected.approxEquals(actual, tolerance),
        //             """
        //             |Expected:
        //             |$expected
        //             |Actual:
        //             |$actual
        //             """.trimMargin()
        //         )
        //     }
        // )
    }

    @Test
    fun `test setting limb tip transform with rotation on base world transform`() {
        TODO()
        // val base = makeBase(FrameTransformation.fromTranslation(10, 0, -10))
        //
        // base.setCurrentWorldSpaceTransform(
        //     FrameTransformation.fromTranslation(10, 0, 0) *
        //         FrameTransformation.fromRotation(0, 90, 0)
        // )
        //
        // val desiredTipTransform = FrameTransformation.fromTranslation(25, 0, -15)
        // base.setDesiredLimbTipTransform(
        //     limb.id,
        //     desiredTipTransform,
        //     BasicMotionConstraints(1, 0, 0, 0)
        // )
        //
        // assertAll(
        //     {
        //         val expected = base.getDesiredLimbTipTransform(limb.id)
        //         assertTrue(
        //             expected.approxEquals(desiredTipTransform, tolerance),
        //             """
        //             |Expected:
        //             |$expected
        //             |Actual:
        //             |$desiredTipTransform
        //             """.trimMargin()
        //         )
        //     },
        //     {
        //         val expected = FrameTransformation.fromTranslation(
        //             x = 25 - 10,
        //             y = 0,
        //             z = -15 - 10 - 10
        //         ) * FrameTransformation.fromRotation(0, -90, 0)
        //         val actual = limb.getDesiredTaskSpaceTransform()
        //         assertTrue(
        //             expected.approxEquals(actual, tolerance),
        //             """
        //             |Expected:
        //             |$expected
        //             |Actual:
        //             |$actual
        //             """.trimMargin()
        //         )
        //     }
        // )
    }

    @Test
    fun `test get current world space transform`() {
        TODO()
        // val mockBodyController = mock<BodyController> {
        //     on { getDeltaSinceLastDesiredTransform() } doReturn
        //         FrameTransformation.fromTranslation(10, 0, 0)
        // }
        //
        // val base = DefaultKinematicBase(
        //     SimpleKinematicBaseId("base"),
        //     immutableListOf(limb),
        //     immutableMapOf(limb.id to FrameTransformation.identity),
        //     mockBodyController
        // )
        //
        // base.setCurrentWorldSpaceTransform(FrameTransformation.fromTranslation(10, 0, 0))
        //
        // assertEquals(
        //     // body controller delta should be added
        //     FrameTransformation.fromTranslation(20, 0, 0),
        //     base.getCurrentWorldSpaceTransformWithDelta()
        // )
    }
}
