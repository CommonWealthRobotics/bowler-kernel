/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
