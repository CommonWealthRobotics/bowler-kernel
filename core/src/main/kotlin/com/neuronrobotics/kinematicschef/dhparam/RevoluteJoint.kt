/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.dhparam

import com.google.common.collect.ImmutableList

/**
 * A revolute joint (pin joint). Has one DH param.
 */
data class RevoluteJoint(override val params: ImmutableList<DhParam>) : DhChainElement {
    init {
        require(params.size == 1) {
            "A revolute joint must have exactly 1 DH param, got ${params.size}."
        }
    }
}
