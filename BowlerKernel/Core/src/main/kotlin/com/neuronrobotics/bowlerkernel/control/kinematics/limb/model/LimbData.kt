/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.limb.model

import com.neuronrobotics.bowlerkernel.control.kinematics.limb.limbid.SimpleLimbId

// TODO: Use a more general "git resource id" instead of just a gist id
// We want to be able to specify files in GitHub repos, too
data class LimbData(
    val id: SimpleLimbId,
    val links: List<LinkData>,
    val forwardKinematicsSolverGistId: String,
    val forwardKinematicsSolverFilename: String,
    val inverseKinematicsSolverGistId: String,
    val inverseKinematicsSolverFilename: String,
    val limbMotionPlanGeneratorGistId: String,
    val limbMotionPlanGeneratorFilename: String,
    val limbMotionPlanFollowerGistId: String,
    val limbMotionPlanFollowerFilename: String,
    val inertialStateEstimatorGistId: String,
    val inertialStateEstimatorFilename: String
)
