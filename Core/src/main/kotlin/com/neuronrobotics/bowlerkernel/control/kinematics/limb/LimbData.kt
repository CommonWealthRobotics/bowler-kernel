/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.limb

import com.neuronrobotics.bowlerkernel.control.kinematics.limb.limbid.SimpleLimbId

data class DhParamData(
    val d: Double,
    val theta: Double,
    val r: Double,
    val alpha: Double
)

// TODO: Use a more general "git resource id" instead of just a gist id
// We want to be able to specify files in GitHub repos, too
data class LimbData(
    val id: SimpleLimbId,
    val chain: List<DhParamData>,
    val forwardKinematicsSolverGistId: String,
    val forwardKinematicsSolverFilename: String,
    val inverseKinematicsSolverGistId: String,
    val inverseKinematicsSolverFilename: String,
    val limbMotionPlanGeneratorGistId: String,
    val limbMotionPlanGeneratorFilename: String,
    val limbMotionPlanFollowerGistId: String,
    val limbMotionPlanFollowerFilename: String
)
