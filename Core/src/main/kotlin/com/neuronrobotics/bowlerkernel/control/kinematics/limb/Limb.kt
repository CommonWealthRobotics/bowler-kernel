/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.limb

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.control.kinematics.FrameTransformation
import com.neuronrobotics.bowlerkernel.control.kinematics.MotionConstraints
import com.neuronrobotics.bowlerkernel.control.kinematics.base.KinematicBase
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.limbid.LimbId
import com.neuronrobotics.kinematicschef.dhparam.DhParam

/**
 * A limb of a robot, mounted to a [KinematicBase].
 */
interface Limb {

    /**
     * The unique (per-[KinematicBase]) id of this limb.
     */
    val id: LimbId

    /**
     * The DH chain that forms this limb.
     */
    val chain: ImmutableList<DhParam>

    /**
     * Sets a desired task space transform this base should try to move to.
     *
     * @param taskSpaceTransform The desired task space transform.
     * @param motionConstraints The constraints on the motion to move from the current task
     * space transform to the desired [taskSpaceTransform].
     */
    fun setDesiredTaskSpaceTransform(
        taskSpaceTransform: FrameTransformation,
        motionConstraints: MotionConstraints
    )

    /**
     * The last set desired task space transform this base should try to move to.
     */
    fun getDesiredTaskSpaceTransform(): FrameTransformation

    /**
     * Sets a joint's desired angle.
     *
     * @param jointIndex The index of the joint.
     * @param jointAngle The new joint angle.
     * @param motionConstraints The constraints of the motion to move from the current joint
     * angle to the desired [jointAngle].
     */
    fun setDesiredJointAngle(
        jointIndex: Int,
        jointAngle: Number,
        motionConstraints: MotionConstraints
    )

    /**
     * The current joint angles.
     */
    fun getCurrentJointAngles(): ImmutableList<Double>
}
