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
import arrow.core.left
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.KinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.BodyController
import com.neuronrobotics.bowlerkernel.kinematics.graph.BaseNode
import com.neuronrobotics.bowlerkernel.kinematics.graph.KinematicGraph
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.LimbId
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints
import org.octogonapus.ktguava.collections.outNodes
import org.octogonapus.ktguava.collections.outNodesAndEdges
import org.octogonapus.ktguava.collections.toImmutableMap
import org.octogonapus.ktguava.collections.toImmutableSet

/**
 * A [KinematicBase] which only considers limbs directly attached to it.
 *
 * @param id The id of this base.
 * @param bodyController The body controller this base uses.
 * @param limbs The limbs attached directly to this base.
 * @param limbBaseTransforms The base transforms of the limbs attached directly to this base.
 * @param imuTransform The transform from the base to the IMU.
 */
@SuppressWarnings("TooManyFunctions")
class DefaultKinematicBase(
    override val id: KinematicBaseId,
    override val bodyController: BodyController,
    val limbs: ImmutableSet<Limb>,
    val limbBaseTransforms: ImmutableMap<LimbId, FrameTransformation>,
    val imuTransform: FrameTransformation
) : KinematicBase {

    private var currentWorldSpaceTransform = FrameTransformation.identity

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
        val limb = limbs.first { it.id == limbId }
        limb.setDesiredTaskSpaceTransform(
            getWorldSpaceTransformInLimbSpace(limbId, worldSpaceTransform),
            motionConstraints
        )
    }

    override fun getCurrentLimbTipTransform(limbId: LimbId): FrameTransformation {
        val limb = limbs.first { it.id == limbId }
        return getLimbSpaceTransformInWorldSpace(limbId, limb.getCurrentTaskSpaceTransform())
    }

    override fun getDesiredLimbTipTransform(limbId: LimbId): FrameTransformation {
        val limb = limbs.first { it.id == limbId }
        return getLimbSpaceTransformInWorldSpace(limbId, limb.getDesiredTaskSpaceTransform())
    }

    override fun getWorldSpaceTransformInLimbSpace(
        limbId: LimbId,
        worldSpaceTransform: FrameTransformation
    ) = worldSpaceTransform *
        getCurrentWorldSpaceTransformWithDelta().inverse *
        (limbBaseTransforms[limbId] ?: error("")).inverse

    override fun getLimbSpaceTransformInWorldSpace(
        limbId: LimbId,
        limbSpaceTransform: FrameTransformation
    ) = limbSpaceTransform *
        (limbBaseTransforms[limbId] ?: error("")) *
        getCurrentWorldSpaceTransformWithDelta()

    override fun computeJacobian(limbId: LimbId, linkIndex: Int): Matrix {
        TODO("not implemented")
    }

    override fun getInertialState() = bodyController.getInertialState()

    companion object {

        /**
         * Creates a [DefaultKinematicBase] using a [KinematicGraph]. Only considers the limbs
         * attached directly to the base in the graph.
         *
         * @param kinematicGraph The graph containing the base and the limbs attached to it.
         * @param baseId The id of this base.
         * @param bodyController The body controller this base uses.
         * @param imuTransform The transform from the base to the IMU.
         */
        fun create(
            kinematicGraph: KinematicGraph,
            baseId: KinematicBaseId,
            bodyController: BodyController,
            imuTransform: FrameTransformation
        ): DefaultKinematicBase {
            val limbs = kinematicGraph.outNodes(BaseNode(baseId).left()).filter {
                it.isRight()
            }.mapTo(hashSetOf()) {
                (it as Either.Right).b
            }

            val limbBaseTransforms =
                kinematicGraph.outNodesAndEdges(BaseNode(baseId).left()).filter {
                    it.first.nodeV() is Either.Right
                }.map {
                    (it.first.nodeV() as Either.Right).b.id to it.second
                }.toMap()

            return DefaultKinematicBase(
                baseId,
                bodyController,
                limbs.toImmutableSet(),
                limbBaseTransforms.toImmutableMap(),
                imuTransform
            )
        }
    }
}
