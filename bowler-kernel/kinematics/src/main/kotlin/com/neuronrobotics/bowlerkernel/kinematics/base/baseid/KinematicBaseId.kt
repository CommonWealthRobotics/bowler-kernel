/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.kinematics.base.baseid

import com.neuronrobotics.bowlerkernel.kinematics.base.KinematicBase

/**
 * The id of a [KinematicBase].
 */
interface KinematicBaseId {

    /**
     * The value of this id as a string.
     */
    override fun toString(): String
}
