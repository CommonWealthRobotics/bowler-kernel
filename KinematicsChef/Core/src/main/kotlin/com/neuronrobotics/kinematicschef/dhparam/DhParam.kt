/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.dhparam

import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.util.toImmutableList
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DHLink

internal data class DhParam(
    val d: Double,
    val theta: Double,
    val r: Double,
    val alpha: Double
) {
    companion object {
        val zero = DhParam(0.0, 0.0, 0.0, 0.0)
    }
}

internal fun DHLink.toDhParam() = DhParam(d, theta, r, theta)

internal fun ImmutableList<DHLink>.toDhParams() = map { it.toDhParam() }.toImmutableList()

internal fun DHChain.toDhParams() = links.toImmutableList().toDhParams()
