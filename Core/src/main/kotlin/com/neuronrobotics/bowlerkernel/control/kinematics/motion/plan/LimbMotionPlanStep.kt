/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.motion.plan

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.MotionConstraints

/**
 * One step of a limb motion plan.
 *
 * @param jointAngles The joint angles for the timestep.
 * @param motionConstraints The constraints on the motion to move from the previous joint angles
 * to the new [jointAngles].
 */
data class LimbMotionPlanStep(
    val jointAngles: ImmutableList<Double>,
    val motionConstraints: MotionConstraints
)
