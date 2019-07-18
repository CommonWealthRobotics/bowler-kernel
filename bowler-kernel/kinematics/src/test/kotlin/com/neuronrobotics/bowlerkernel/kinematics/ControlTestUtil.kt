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

import arrow.core.left
import arrow.core.right
import com.beust.klaxon.Klaxon
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.KinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseScriptData
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopBodyController
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopJointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.graph.BaseNode
import com.neuronrobotics.bowlerkernel.kinematics.graph.KinematicGraph
import com.neuronrobotics.bowlerkernel.kinematics.graph.buildMutableKinematicGraph
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DefaultLink
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.DhParamData
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.LinkConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.LinkScriptData
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
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.toImmutableNetwork

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
    ).right()
)

internal fun kinematicBaseConfigurationData() = KinematicBaseConfigurationData(
    "A"
)

internal val seaArmLinks: ImmutableList<Link> = immutableListOf(
    DefaultLink(
        LinkType.Rotary,
        DhParam(135, 0, 0, -90),
        NoopInertialStateEstimator
    ),
    DefaultLink(
        LinkType.Rotary,
        DhParam(0, 0, 175, 0),
        NoopInertialStateEstimator
    ),
    DefaultLink(
        LinkType.Rotary,
        DhParam(0, 90, 169.28, 0),
        NoopInertialStateEstimator
    )
)

internal fun makeSimpleKinematicGraph(
    baseId: KinematicBaseId,
    vararg limbs: Pair<Limb, FrameTransformation>
): KinematicGraph {
    val mutableGraph = buildMutableKinematicGraph()

    limbs.forEach { (limb, transform) ->
        mutableGraph.addEdge(BaseNode(baseId).left(), limb.right(), transform)
    }

    return mutableGraph.toImmutableNetwork()
}
