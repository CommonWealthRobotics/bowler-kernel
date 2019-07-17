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
@file:Suppress("UnstableApiUsage")

package com.neuronrobotics.bowlerkernel.kinematics.graph

import arrow.core.Either
import arrow.core.Tuple2
import arrow.core.Tuple3
import arrow.core.right
import com.beust.klaxon.Klaxon
import com.google.common.graph.ImmutableNetwork
import com.google.common.graph.MutableNetwork
import com.neuronrobotics.bowlerkernel.kinematics.base.KinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.KinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseScriptData
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.DhParamData
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.LinkConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.LinkScriptData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbScriptData
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.ClassData

data class BaseNode(
    val id: KinematicBaseId
)

typealias KinematicGraph = ImmutableNetwork<Either<BaseNode, Limb>, FrameTransformation>
typealias MutableKinematicGraph = MutableNetwork<Either<BaseNode, Limb>, FrameTransformation>

fun KinematicGraph.convertToKinematicGraphData(
    bases: Set<KinematicBase>,
    klaxon: Klaxon = Klaxon().converter(FrameTransformation)
): KinematicGraphData {
    val mappedNodes = nodes().map {
        it to it.bimap(
            { baseNode -> bases.first { it.id == baseNode.id } },
            { it }
        ).mapToKinematicGraphDataNode(klaxon)
    }.toMap()

    return KinematicGraphData(
        mappedNodes.values.toList(),
        fullEdges().map { (nodeU, nodeV, edge) ->
            Tuple3(
                mappedNodes[nodeU] ?: error(""),
                mappedNodes[nodeV] ?: error(""),
                edge
            )
        }
    )
}

private fun Either<KinematicBase, Limb>.mapToKinematicGraphDataNode(
    klaxon: Klaxon
): KinematicGraphDataNode = bimap(
    {
        Tuple2(
            KinematicBaseConfigurationData(it.id.toString()),
            KinematicBaseScriptData(
                ClassData.fromInstance(it.bodyController, klaxon).right()
            )
        )
    },
    { limb ->
        Tuple2(
            LimbConfigurationData(
                limb.id.toString(),
                limb.links.map { link ->
                    LinkConfigurationData(link.type, DhParamData(link.dhParam))
                }
            ),
            LimbScriptData(
                ClassData.fromInstance(limb.forwardKinematicsSolver, klaxon).right(),
                ClassData.fromInstance(limb.inverseKinematicsSolver, klaxon).right(),
                ClassData.fromInstance(limb.reachabilityCalculator, klaxon).right(),
                ClassData.fromInstance(limb.motionPlanGenerator, klaxon).right(),
                ClassData.fromInstance(limb.motionPlanFollower, klaxon).right(),
                ClassData.fromInstance(limb.inertialStateEstimator, klaxon).right(),
                limb.links.mapIndexed { index, link ->
                    LinkScriptData(
                        ClassData.fromInstance(
                            limb.jointAngleControllers[index],
                            klaxon
                        ).right(),
                        ClassData.fromInstance(link.inertialStateEstimator, klaxon).right()
                    )
                }
            )
        )
    }
)
