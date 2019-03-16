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

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.KinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.BodyController
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.LimbId
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints
import org.ejml.simple.SimpleMatrix

class DefaultKinematicBase(
    override val id: KinematicBaseId,
    override val limbs: ImmutableList<Limb>,
    override val limbBaseTransforms: ImmutableMap<LimbId, FrameTransformation>,
    override val bodyController: BodyController
) : KinematicBase {

    private var currentWorldSpaceTransform = FrameTransformation.identity

    override fun setDesiredWorldSpaceTransformDelta(
        worldSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    ) {
        bodyController.setDesiredWorldSpaceTransformDelta(worldSpaceTransform, motionConstraints)
    }

    override fun setWorldSpaceTransform(worldSpaceTransform: FrameTransformation) {
        currentWorldSpaceTransform = worldSpaceTransform
    }

    override fun getCurrentWorldSpaceTransform() =
        currentWorldSpaceTransform + bodyController.getDeltaSinceLastDesiredTransform()

    override fun computeJacobian(limbIndex: Int, linkIndex: Int): SimpleMatrix {
        TODO("not implemented")
    }

    override fun getInertialState() = bodyController.getInertialState()
}
