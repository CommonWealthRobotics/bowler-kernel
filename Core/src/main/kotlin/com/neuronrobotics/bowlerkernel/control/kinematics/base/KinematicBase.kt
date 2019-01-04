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
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.InertialState
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.MotionConstraints
import org.ejml.simple.SimpleMatrix

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
     * Sets a desired world space transform delta this base should try to move by.
     *
     * @param worldSpaceTransform The desired world space transform delta.
     * @param motionConstraints The constraints on the motion to move from the current world
     * space transform to the desired [worldSpaceTransform].
     */
    fun setDesiredWorldSpaceTransformDelta(
        worldSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    )

    /**
     * Sets the current world space transform. This can be used for error correction if the
     * transform can be estimated externally or can be used to set a starting position.
     *
     * @param worldSpaceTransform The current world space transform.
     */
    fun setWorldSpaceTransform(worldSpaceTransform: FrameTransformation)

    /**
     * The current (estimated) world space transform.
     */
    fun getCurrentWorldSpaceTransform(): FrameTransformation

    /**
     * Computes the current Jacobian matrix for the given link.
     *
     * @param limbIndex The index of the limb in [limbs].
     * @param linkIndex The index of the link in [Limb.links].
     * @return The Jacobian matrix.
     */
    fun computeJacobian(limbIndex: Int, linkIndex: Int): SimpleMatrix

    /**
     * Returns the current [InertialState] for this base.
     *
     * @return The current [InertialState].
     */
    fun getInertialState(): InertialState
}
