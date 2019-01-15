/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.kinematics.motion.plan

import com.google.common.collect.ImmutableList

/**
 * A full limb motion plan.
 *
 * @param steps The steps in the plan.
 */
data class LimbMotionPlan(
    val steps: ImmutableList<LimbMotionPlanStep>
)
