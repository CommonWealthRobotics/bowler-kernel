/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.kinematics.limb

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbData

interface LimbFactory {

    /**
     * Creates a new limb from the [limbData].
     *
     * @param limbData The data to construct the limb from.
     * @return A new limb.
     */
    fun createLimb(limbData: LimbData): Either<LimbCreationError, Limb>
}
