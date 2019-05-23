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
import com.neuronrobotics.bowlerkernel.kinematics.motion.InertialState
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints
import org.ejml.simple.SimpleMatrix

/**
 * The main "base" of a robot. Generally, it is the central hub of the robot to which the limbs
 * are attached.
 */
@SuppressWarnings("ComplexInterface", "TooManyFunctions")
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
     * Sets the current world space transform, not including the
     * [BodyController.getDeltaSinceLastDesiredTransform]. This can be used for error correction
     * if the transform can be estimated externally or can be used to set a starting position.
     *
     * @param worldSpaceTransform The current world space transform.
     */
    fun setCurrentWorldSpaceTransform(worldSpaceTransform: FrameTransformation)

    /**
     * The current world space transform including the
     * [BodyController.getDeltaSinceLastDesiredTransform].
     */
    fun getCurrentWorldSpaceTransformWithDelta(): FrameTransformation

    /**
     * Sets a desired world space transform the limb tip should try to move to.
     *
     * @param limbId The id of the limb.
     * @param worldSpaceTransform The desired world space transform for the limb tip.
     * @param motionConstraints The constraints on the motion to move from the current task
     * space transform to the desired [worldSpaceTransform].
     */
    fun setDesiredLimbTipTransform(
        limbId: LimbId,
        worldSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    )

    /**
     * Sets a desired world space transform the limb tip should try to move to.
     *
     * @param limbIndex The index of the limb in [limbs].
     * @param worldSpaceTransform The desired world space transform for the limb tip.
     * @param motionConstraints The constraints on the motion to move from the current task
     * space transform to the desired [worldSpaceTransform].
     */
    fun setDesiredLimbTipTransform(
        limbIndex: Int,
        worldSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    )

    /**
     * Reads the current tip transform of the limb in world space.
     *
     * @param limbId The id of the limb.
     * @return The current limb tip transform in world space.
     */
    fun getCurrentLimbTipTransform(limbId: LimbId): FrameTransformation

    /**
     * Reads the current tip transform of the limb in world space.
     *
     * @param limbIndex The index of the limb in [limbs].
     * @return The current limb tip transform in world space.
     */
    fun getCurrentLimbTipTransform(limbIndex: Int): FrameTransformation

    /**
     * Reads the desired tip transform of the limb in world space.
     *
     * @param limbId The id of the limb.
     * @return The desired limb tip transform in world space.
     */
    fun getDesiredLimbTipTransform(limbId: LimbId): FrameTransformation

    /**
     * Reads the desired tip transform of the limb in world space.
     *
     * @param limbIndex The index of the limb in [limbs].
     * @return The desired limb tip transform in world space.
     */
    fun getDesiredLimbTipTransform(limbIndex: Int): FrameTransformation

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
