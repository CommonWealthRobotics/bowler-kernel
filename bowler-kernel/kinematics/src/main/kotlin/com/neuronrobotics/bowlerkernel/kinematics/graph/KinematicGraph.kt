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
import com.google.common.graph.ImmutableNetwork
import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import com.neuronrobotics.bowlerkernel.kinematics.base.KinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.base.KinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation

/**
 * An identifier of a [KinematicBase] in a [KinematicGraph].
 *
 * @param id The [KinematicBase.id].
 */
data class BaseNode(val id: KinematicBaseId)

typealias KinematicGraph = ImmutableNetwork<Either<BaseNode, Limb>, FrameTransformation>
typealias MutableKinematicGraph = MutableNetwork<Either<BaseNode, Limb>, FrameTransformation>

/**
 * @return An empty [MutableKinematicGraph].
 */
fun buildMutableKinematicGraph(): MutableKinematicGraph =
    NetworkBuilder.directed()
        .allowsParallelEdges(false)
        .allowsSelfLoops(false)
        .build()
