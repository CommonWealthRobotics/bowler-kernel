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
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.KinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.LimbId
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints

/**
 * A [KinematicBase] which only considers limbs directly attached to it.
 *
 * @param id The id of this base.
 * @param limbs The limbs attached directly to this base.
 * @param limbBaseTransforms The base transforms of the limbs attached directly to this base.
 */
@SuppressWarnings("TooManyFunctions")
class DefaultKinematicBase(
    override val id: KinematicBaseId,
    val limbs: ImmutableSet<Limb>,
    val limbBaseTransforms: ImmutableMap<LimbId, FrameTransformation>
) : KinematicBase {

    override fun setDesiredLimbTipTransform(
        limbId: LimbId,
        worldSpaceTransform: FrameTransformation,
        currentBodyTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    ) {
        val limb = limbs.first { it.id == limbId }
        limb.setDesiredTaskSpaceTransform(
            getWorldSpaceTransformInLimbSpace(limbId, worldSpaceTransform, currentBodyTransform),
            motionConstraints
        )
    }

    override fun getCurrentLimbTipTransform(
        limbId: LimbId,
        currentBodyTransform: FrameTransformation
    ): FrameTransformation {
        val limb = limbs.first { it.id == limbId }
        return getLimbSpaceTransformInWorldSpace(
            limbId,
            limb.getCurrentTaskSpaceTransform(),
            currentBodyTransform
        )
    }

    override fun getDesiredLimbTipTransform(
        limbId: LimbId,
        currentBodyTransform: FrameTransformation
    ): FrameTransformation {
        val limb = limbs.first { it.id == limbId }
        return getLimbSpaceTransformInWorldSpace(
            limbId,
            limb.getDesiredTaskSpaceTransform(),
            currentBodyTransform
        )
    }

    override fun getWorldSpaceTransformInLimbSpace(
        limbId: LimbId,
        worldSpaceTransform: FrameTransformation,
        currentBodyTransform: FrameTransformation
    ) = worldSpaceTransform *
        currentBodyTransform.inverse *
        (limbBaseTransforms[limbId] ?: error("")).inverse

    override fun getLimbSpaceTransformInWorldSpace(
        limbId: LimbId,
        limbSpaceTransform: FrameTransformation,
        currentBodyTransform: FrameTransformation
    ) = limbSpaceTransform *
        (limbBaseTransforms[limbId] ?: error("")) *
        currentBodyTransform

    override fun computeJacobian(limbId: LimbId, linkIndex: Int): Matrix {
        TODO("not implemented")
    }
}
