/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.kinematics.limb.model

import com.neuronrobotics.bowlerkernel.kinematics.Limits
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType

data class LinkData(
    val type: LinkType,
    val dhParamData: DhParamData,
    val jointLimits: Limits,
    val jointAngleControllerGistId: String,
    val jointAngleControllerFilename: String,
    val inertialStateEstimatorGistId: String,
    val inertialStateEstimatorFilename: String
)
