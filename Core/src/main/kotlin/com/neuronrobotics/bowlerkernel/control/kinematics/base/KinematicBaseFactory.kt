package com.neuronrobotics.bowlerkernel.control.kinematics.base

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.control.kinematics.base.model.KinematicBaseData

interface KinematicBaseFactory {

    /**
     * Creates a new kinematic base from the [kinematicBaseData].
     *
     * @param kinematicBaseData The data to construct the base from.
     * @return A new kinematic base.
     */
    fun create(
        kinematicBaseData: KinematicBaseData
    ): Either<KinematicBaseCreationError, KinematicBase>
}
