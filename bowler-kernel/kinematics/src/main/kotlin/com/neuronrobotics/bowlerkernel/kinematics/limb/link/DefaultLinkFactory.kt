/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.kinematics.limb.link

import com.neuronrobotics.bowlerkernel.kinematics.Limits
import com.neuronrobotics.bowlerkernel.kinematics.motion.InertialStateEstimator
import com.neuronrobotics.kinematicschef.dhparam.DhParam

class DefaultLinkFactory : LinkFactory {

    override fun createLink(
        type: LinkType,
        dhParam: DhParam,
        jointLimits: Limits,
        inertialStateEstimator: InertialStateEstimator
    ) = DefaultLink(
        type,
        dhParam,
        jointLimits,
        inertialStateEstimator
    )
}
