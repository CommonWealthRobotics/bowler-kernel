/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.limb.link

import com.neuronrobotics.bowlerkernel.control.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.InertialStateEstimator
import com.neuronrobotics.bowlerkernel.util.Limits
import com.neuronrobotics.kinematicschef.dhparam.DhParam

/**
 * A link which can form the chain of a [Limb].
 */
interface Link {

    /**
     * The type of this link.
     */
    val type: LinkType

    /**
     * The DH description of this link.
     */
    val dhParam: DhParam

    /**
     * The movement limits of this link. This can represent angle for a rotary link or length for
     * a prismatic link.
     */
    val jointLimits: Limits

    /**
     * The [InertialStateEstimator].
     */
    val inertialStateEstimator: InertialStateEstimator
}
