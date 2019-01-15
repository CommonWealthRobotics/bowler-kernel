/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.kinematics.limb.link

import com.neuronrobotics.bowlerkernel.kinematics.Limits
import com.neuronrobotics.bowlerkernel.kinematics.motion.InertialStateEstimator

data class DefaultLink(
    override val type: LinkType,
    override val dhParam: DhParam,
    override val jointLimits: Limits,
    override val inertialStateEstimator: InertialStateEstimator
) : Link