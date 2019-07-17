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

package com.neuronrobotics.bowlerkernel.kinematics.base

import Jama.Matrix
import arrow.core.Either
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.KinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.BodyController
import com.neuronrobotics.bowlerkernel.kinematics.graph.KinematicGraph
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.LimbId
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints

@SuppressWarnings("TooManyFunctions")
class DefaultKinematicBase(
    override val id: KinematicBaseId,
    override val kinematicGraph: KinematicGraph,
    override val bodyController: BodyController
) : KinematicBase {

    private var currentWorldSpaceTransform = FrameTransformation.identity
    private val nodes by lazy {
        kinematicGraph.nodes()
    }

    override fun setDesiredWorldSpaceTransformDelta(
        worldSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    ) {
        bodyController.setDesiredWorldSpaceTransformDelta(worldSpaceTransform, motionConstraints)
    }

    override fun setCurrentWorldSpaceTransform(worldSpaceTransform: FrameTransformation) {
        currentWorldSpaceTransform = worldSpaceTransform
    }

    override fun getCurrentWorldSpaceTransformWithDelta() =
        currentWorldSpaceTransform * bodyController.getDeltaSinceLastDesiredTransform()

    override fun setDesiredLimbTipTransform(
        limbId: LimbId,
        worldSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    ) {
        TODO()
        // val limb = nodes.firstLimb { it.id == limbId }
        // limb.setDesiredTaskSpaceTransform(
        //     worldSpaceTransform *
        //         getCurrentWorldSpaceTransformWithDelta().inverse *
        //         limbBaseTransforms[limb.id]!!.inverse,
        //     motionConstraints
        // )
    }

    override fun getCurrentLimbTipTransform(limbId: LimbId): FrameTransformation {
        TODO()
        // val limb = nodes.firstLimb { it.id == limbId }
        // return limb.getCurrentTaskSpaceTransform() *
        //     limbBaseTransforms[limb.id]!! *
        //     getCurrentWorldSpaceTransformWithDelta()
    }

    override fun getDesiredLimbTipTransform(limbId: LimbId): FrameTransformation {
        TODO()
        // val limb = nodes.firstLimb { it.id == limbId }
        // return limb.getDesiredTaskSpaceTransform() *
        //     limbBaseTransforms[limb.id]!! *
        //     getCurrentWorldSpaceTransformWithDelta()
    }

    override fun computeJacobian(limbId: LimbId, linkIndex: Int): Matrix {
        TODO("not implemented")
    }

    override fun getInertialState() = bodyController.getInertialState()
}

private fun Collection<Either<KinematicBase, Limb>>.firstLimb(
    predicate: (Limb) -> Boolean
): Limb = (first {
    it is Either.Right && predicate(it.b)
} as Either.Right).b
