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
package com.neuronrobotics.bowlerkernel.kinematics

import arrow.core.right
import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseScriptData
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopBodyController
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopJointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.LinkConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.LinkScriptData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.DhParamData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbScriptData
import com.neuronrobotics.bowlerkernel.kinematics.motion.BasicMotionConstraints
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.LengthBasedReachabilityCalculator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.ClassData
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanGenerator
import org.junit.jupiter.api.Assertions.assertEquals

internal fun createMotionConstraints(duration: Number) = BasicMotionConstraints(
    duration, 0, 0, 0
)

internal inline fun <reified T> Klaxon.testJsonConversion(input: T) {
    assertEquals(input, parse<T>(toJsonString(input).also { println(it) }))
}

internal fun linkConfigurationData() = LinkConfigurationData(
    LinkType.Rotary,
    DhParamData(1, 2, 3, 4.5)
)

internal fun Klaxon.linkScriptData() = LinkScriptData(
    ClassData.fromInstance(
        NoopJointAngleController,
        this
    ).right(),
    ClassData.fromInstance(
        NoopInertialStateEstimator,
        this
    ).right()
)

internal fun limbConfigurationData() = LimbConfigurationData(
    "A",
    listOf(
        linkConfigurationData(),
        linkConfigurationData()
    )
)

internal fun Klaxon.limbScriptData() = LimbScriptData(
    ClassData.fromInstance(
        NoopForwardKinematicsSolver,
        this
    ).right(),
    ClassData.fromInstance(
        NoopInverseKinematicsSolver,
        this
    ).right(),
    ClassData.fromInstance(
        LengthBasedReachabilityCalculator(),
        this
    ).right(),
    ClassData.fromInstance(
        NoopLimbMotionPlanGenerator,
        this
    ).right(),
    ClassData.fromInstance(
        NoopLimbMotionPlanFollower,
        this
    ).right(),
    ClassData.fromInstance(
        NoopInertialStateEstimator,
        this
    ).right(),
    listOf(
        linkScriptData(),
        linkScriptData()
    )
)

internal fun Klaxon.kinematicBaseScriptData() = KinematicBaseScriptData(
    ClassData.fromInstance(
        NoopBodyController,
        this
    ).right(),
    listOf(
        limbScriptData(),
        limbScriptData()
    )
)

internal fun kinematicBaseConfigurationData() = KinematicBaseConfigurationData(
    "A",
    listOf(
        limbConfigurationData(),
        limbConfigurationData()
    ),
    listOf(
        FrameTransformation.fromTranslation(10, 20, 30),
        FrameTransformation.identity
    )
)
