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

import Jama.Matrix
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.KinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.BodyController
import com.neuronrobotics.bowlerkernel.kinematics.graph.KinematicGraph
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.LimbId
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.InertialState
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints

/**
 * A [KinematicBase] which uses all bases and limbs in the graph.
 *
 * @param id The id of this base.
 * @param bodyController The body controller this base uses.
 * @param kinematicGraph The kinematic graph containing the base and any other bases or limbs.
 */
@Suppress("UnstableApiUsage")
class GeneralKinematicBase(
    override val id: KinematicBaseId,
    override val bodyController: BodyController,
    private val kinematicGraph: KinematicGraph
) : KinematicBase {

    override fun setDesiredWorldSpaceTransformDelta(
        worldSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    ) {
        TODO("not implemented")
    }

    override fun setCurrentWorldSpaceTransform(worldSpaceTransform: FrameTransformation) {
        TODO("not implemented")
    }

    override fun getCurrentWorldSpaceTransformWithDelta(): FrameTransformation {
        TODO("not implemented")
    }

    override fun setDesiredLimbTipTransform(
        limbId: LimbId,
        worldSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    ) {
        TODO("not implemented")
    }

    override fun getCurrentLimbTipTransform(limbId: LimbId): FrameTransformation {
        TODO("not implemented")
    }

    override fun getDesiredLimbTipTransform(limbId: LimbId): FrameTransformation {
        TODO("not implemented")
    }

    override fun getWorldSpaceTransformInLimbSpace(
        limbId: LimbId,
        worldSpaceTransform: FrameTransformation
    ): FrameTransformation {
        TODO("not implemented")
    }

    override fun getLimbSpaceTransformInWorldSpace(
        limbId: LimbId,
        limbSpaceTransform: FrameTransformation
    ): FrameTransformation {
        TODO("not implemented")
    }

    override fun computeJacobian(limbId: LimbId, linkIndex: Int): Matrix {
        TODO("not implemented")
    }

    override fun getInertialState(): InertialState {
        TODO("not implemented")
    }
}
