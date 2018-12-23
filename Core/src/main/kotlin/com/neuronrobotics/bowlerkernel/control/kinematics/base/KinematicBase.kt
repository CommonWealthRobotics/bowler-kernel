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

/**
 * The main "base" of a robot. Generally, it is the central hub of the robot to which the limbs
 * are attached.
 */
interface KinematicBase {

    /**
     * The unique id of this base.
     */
    val id: KinematicBaseId

    /**
     * The limbs attached to this base.
     */
    val limbs: ImmutableList<Limb>

    /**
     * A mapping of a [Limb.id] to the [FrameTransformation] which moves its starting point to
     * its mounting point on this base.
     */
    val limbBaseTransforms: ImmutableMap<LimbId, FrameTransformation>

    /**
     * The controller for the body.
     */
    val bodyController: BodyController

    /**
     * Sets a desired world space transform this base should try to move to.
     *
     * @param worldSpaceTransform The desired world space transform.
     * @param motionConstraints The constraints on the motion to move from the current world
     * space transform to the desired [worldSpaceTransform].
     */
    fun setDesiredWorldSpaceTransform(
        worldSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    )

    /**
     * The last set desired world space transform this base should try to move to.
     */
    fun getDesiredWorldSpaceTransform(): FrameTransformation

    /**
     * The current (estimated) world space transform.
     */
    fun getCurrentWorldSpaceTransform(): FrameTransformation
}
