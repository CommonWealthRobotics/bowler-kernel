/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import com.google.common.collect.ImmutableList
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DHLink

internal interface WristIdentifier {

    /**
     * Computes whether the [chain] is a spherical wrist.
     *
     * @param chain The [DHChain] to analyze.
     * @return True if the [chain] forms a spherical wrist, false otherwise.
     */
    fun isSphericalWrist(chain: ImmutableList<DHLink>): Boolean
}
