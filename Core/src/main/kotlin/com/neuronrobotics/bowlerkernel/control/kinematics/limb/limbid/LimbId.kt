/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.limb.limbid

import com.neuronrobotics.bowlerkernel.control.kinematics.limb.Limb

/**
 * The id of a [Limb].
 */
interface LimbId {

    /**
     * The value of this id as a string.
     */
    override fun toString(): String
}
