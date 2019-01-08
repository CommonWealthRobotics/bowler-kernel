/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.base

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.neuronrobotics.bowlerkernel.control.closedloop.BodyController
import com.neuronrobotics.bowlerkernel.control.kinematics.base.baseid.KinematicBaseId
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.limbid.LimbId
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.MotionConstraints
import org.ejml.simple.SimpleMatrix

class DefaultKinematicBase(
    override val id: KinematicBaseId,
    override val limbs: ImmutableList<Limb>,
    override val limbBaseTransforms: ImmutableMap<LimbId, FrameTransformation>,
    override val bodyController: BodyController
) : KinematicBase {

    private var currentWorldSpaceTransform = FrameTransformation.identity()

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
