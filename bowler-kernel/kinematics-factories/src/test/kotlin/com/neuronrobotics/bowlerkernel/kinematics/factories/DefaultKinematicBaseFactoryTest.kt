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
package com.neuronrobotics.bowlerkernel.kinematics.factories

import arrow.core.Either
import arrow.core.right
import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.kinematics.base.DefaultKinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.SimpleKinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseScriptData
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopBodyController
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopJointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.limb.DefaultLimb
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DefaultLink
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.DhParamData
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.LinkConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.LinkScriptData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbScriptData
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.LengthBasedReachabilityCalculator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.ClassData
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.scripting.factory.DefaultGitScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.parser.DefaultScriptLanguageParser
import com.nhaarman.mockitokotlin2.mock
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableMapOf
import org.octogonapus.ktguava.collections.immutableSetOf

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultKinematicBaseFactoryTest {

    private val klaxon = Klaxon().converter(FrameTransformation)

    private val linkScriptData = LinkScriptData(
        ClassData.fromInstance(NoopJointAngleController, klaxon).right(),
        ClassData.fromInstance(NoopInertialStateEstimator, klaxon).right()
    )

    private val limbConfigurationData = LimbConfigurationData(
        "limb 1 id",
        listOf(
            LinkConfigurationData(
                LinkType.Rotary,
                DhParamData(10, 20, 30, 40)
            )
        )
    )

    private val limbBaseTransform =
        SimpleLimbId("limb 1 id") to FrameTransformation.fromTranslation(10, 20, 30)

    private val limbScriptData = LimbScriptData(
        ClassData.fromInstance(NoopForwardKinematicsSolver, klaxon).right(),
        ClassData.fromInstance(NoopInverseKinematicsSolver, klaxon).right(),
        ClassData.fromInstance(LengthBasedReachabilityCalculator(), klaxon).right(),
        ClassData.fromInstance(NoopLimbMotionPlanGenerator, klaxon).right(),
        ClassData.fromInstance(NoopLimbMotionPlanFollower, klaxon).right(),
        ClassData.fromInstance(NoopInertialStateEstimator, klaxon).right(),
        listOf(linkScriptData)
    )

    private val configData = KinematicBaseConfigurationData("base id")

    private val scriptData = KinematicBaseScriptData(
        ClassData.fromInstance(NoopBodyController, klaxon).right()
    )

    private val gitScriptFactory = DefaultGitScriptFactory(
        mock {},
        DefaultScriptLanguageParser()
    )

    private val factory = DefaultKinematicBaseFactory(
        gitScriptFactory,
        DefaultLimbFactory(
            gitScriptFactory,
            DefaultLinkFactory(gitScriptFactory, klaxon),
            klaxon
        ),
        klaxon
    )

    @Test
    fun `full base test`() {
        val expected = DefaultKinematicBase(
            SimpleKinematicBaseId("base id"),
            NoopBodyController,
            immutableSetOf(
                DefaultLimb(
                    SimpleLimbId("limb 1 id"),
                    immutableListOf(
                        DefaultLink(
                            LinkType.Rotary,
                            DhParam(10, 20, 30, 40),
                            NoopInertialStateEstimator
                        )
                    ),
                    NoopForwardKinematicsSolver,
                    NoopInverseKinematicsSolver,
                    LengthBasedReachabilityCalculator(),
                    NoopLimbMotionPlanGenerator,
                    NoopLimbMotionPlanFollower,
                    immutableListOf(NoopJointAngleController),
                    NoopInertialStateEstimator
                )
            ),
            immutableMapOf(
                SimpleLimbId("limb 1 id") to
                    FrameTransformation.fromTranslation(10, 20, 30)
            ),
            FrameTransformation.fromTranslation(1, 2, 3)
        )

        val actual = factory.create(
            configData,
            scriptData,
            listOf(limbConfigurationData to limbScriptData),
            listOf(limbBaseTransform).toMap(),
            FrameTransformation.fromTranslation(1, 2, 3)
        )

        assertTrue(actual is Either.Right, "actual was $actual")
        actual as Either.Right
        val base = actual.b as DefaultKinematicBase

        assertAll(
            { assertEquals(expected::class, base::class) },
            { assertEquals(expected.id, base.id) },
            { assertEquals(expected.limbs.size, base.limbs.size) },
            { assertEquals(expected.limbs.first().id, base.limbs.first().id) },
            { assertEquals(expected.limbs.first().links, base.limbs.first().links) },
            { assertEquals(expected.limbBaseTransforms.entries, base.limbBaseTransforms.entries) },
            { assertEquals(expected.bodyController::class, base.bodyController::class) },
            { assertEquals(expected.imuTransform, base.imuTransform) }
        )
    }
}
