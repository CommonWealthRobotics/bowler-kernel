/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.kinematics.base.model

import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.SimpleKinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbData
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation

data class KinematicBaseData(
    val id: SimpleKinematicBaseId,
    val limbs: List<LimbData>,
    val limbTransforms: List<FrameTransformation>,
    val bodyControllerGistId: String,
    val bodyControllerFilename: String
)
