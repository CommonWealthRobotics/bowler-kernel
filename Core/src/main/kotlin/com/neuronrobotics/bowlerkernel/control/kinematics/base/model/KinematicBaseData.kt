package com.neuronrobotics.bowlerkernel.control.kinematics.base.model

import com.neuronrobotics.bowlerkernel.control.kinematics.base.baseid.SimpleKinematicBaseId
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.model.LimbData
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.FrameTransformation

data class KinematicBaseData(
    val id: SimpleKinematicBaseId,
    val limbs: List<LimbData>,
    val limbTransforms: List<FrameTransformation>,
    val bodyControllerGistId: String,
    val bodyControllerFilename: String
)
