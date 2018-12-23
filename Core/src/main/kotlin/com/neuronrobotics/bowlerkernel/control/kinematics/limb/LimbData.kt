package com.neuronrobotics.bowlerkernel.control.kinematics.limb

import com.neuronrobotics.bowlerkernel.control.kinematics.limb.limbid.SimpleLimbId

data class DhParamData(
    val d: Double,
    val theta: Double,
    val r: Double,
    val alpha: Double
)

data class LimbData(
    val id: SimpleLimbId,
    val chain: List<DhParamData>,
    val forwardKinematicsSolverPushUrl: String,
    val inverseKinematicsSolverPushUrl: String
)
