/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import com.google.common.collect.ImmutableList
import com.neuronrobotics.sdk.addons.kinematics.DHLink

internal class DefaultWristIdentifier : WristIdentifier {

    override fun isSphericalWrist(chain: ImmutableList<DHLink>): Boolean {
        /**
         * 1. Check there are three links.
         *
         * 2. Map each link to its center of rotation. Check that a center of rotation n+1 is
         * orthogonal to the center of rotation n.
         *
         * Then the wrist is spherical.
         */
        TODO("not implemented")
    }
}
