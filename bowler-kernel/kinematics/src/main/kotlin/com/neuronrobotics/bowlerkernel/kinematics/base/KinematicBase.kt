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
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.kinematics.limb.LimbId
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints

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
        currentBodyTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    )

    /**
     * Reads the current tip transform of the limb in world space.
     *
     * @param limbId The id of the limb.
     * @return The current limb tip transform in world space.
     */
    fun getCurrentLimbTipTransform(
        limbId: LimbId,
        currentBodyTransform: FrameTransformation
    ): FrameTransformation

    /**
     * Reads the desired tip transform of the limb in world space.
     *
     * @param limbId The id of the limb.
     * @return The desired limb tip transform in world space.
     */
    fun getDesiredLimbTipTransform(
        limbId: LimbId,
        currentBodyTransform: FrameTransformation
    ): FrameTransformation

    /**
     * Calculates the value of a world space transform in limb space. This is what
     * [setDesiredLimbTipTransform] does before setting the transform on the limb.
     *
     * @param limbId The id of the limb (will use this limb's limb space).
     * @param worldSpaceTransform The world space transform to map.
     * @return The [worldSpaceTransform] in limb space.
     */
    fun getWorldSpaceTransformInLimbSpace(
        limbId: LimbId,
        worldSpaceTransform: FrameTransformation,
        currentBodyTransform: FrameTransformation
    ): FrameTransformation

    /**
     * Calculates the value of a limb space transform in world space. This is what
     * [getCurrentLimbTipTransform] and [getDesiredLimbTipTransform] do before returning the
     * transform. The world space transform used is the [getCurrentWorldSpaceTransformWithDelta].
     *
     * @param limbId The id of the limb (will use this limb's limb space).
     * @param limbSpaceTransform The limb space transform to map.
     * @return The [limbSpaceTransform] in world space.
     */
    fun getLimbSpaceTransformInWorldSpace(
        limbId: LimbId,
        limbSpaceTransform: FrameTransformation,
        currentBodyTransform: FrameTransformation
    ): FrameTransformation

    /**
     * Computes the current Jacobian matrix for the given link.
     *
     * @param limbId The id of the limb.
     * @param linkIndex The index of the link in [Limb.links].
     * @return The Jacobian matrix.
     */
    fun computeJacobian(limbId: LimbId, linkIndex: Int): Matrix
}
