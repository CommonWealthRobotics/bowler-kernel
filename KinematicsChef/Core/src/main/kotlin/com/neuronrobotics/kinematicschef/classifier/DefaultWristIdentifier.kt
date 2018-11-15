/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import arrow.core.Option
import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhParam

/**
 * 1. Check there are three links.
 *
 * 2. Map each link to its center of rotation. Check that a center of rotation n+1 is
 * orthogonal to the center of rotation n.
 *
 * Then the wrist is spherical.
 */
internal class DefaultWristIdentifier : WristIdentifier {

    override fun isSphericalWrist(chain: ImmutableList<DhParam>): Option<ClassifierError> {
        return if (chain.size == 3) {
            handleChainOfCorrectLength(chain)
        } else {
            Option.just(
                ClassifierError(
                    "A chain of ${chain.size} links cannot form a spherical wrist"
                )
            )
        }
    }

    private fun handleChainOfCorrectLength(chain: ImmutableList<DhParam>): Option<ClassifierError> {
        require(chain.size == 3)

        fun config1() = chain[0].alpha == -90.0 && chain[1].alpha == 90.0
        fun config2() = chain[0].alpha == 90.0 && chain[1].alpha == -90.0
        fun centerLinkNoOffset() = chain[1].r == 0.0 && chain[1].d == 0.0

        return if ((config1() || config2()) && centerLinkNoOffset()) {
            Option.empty()
        } else {
            Option.just(ClassifierError("Not spherical."))
        }
    }
}
