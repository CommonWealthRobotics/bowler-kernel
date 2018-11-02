/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhChainElement
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.RevoluteJoint
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.toImmutableList
import javax.inject.Inject

internal class DefaultChainIdentifier
@Inject internal constructor(
    private val wristIdentifier: WristIdentifier
) : ChainIdentifier {

    override fun identifyChain(chain: ImmutableList<DhParam>): ImmutableList<DhChainElement> {
        /**
         * No spherical wrist can occur with less than 3 links.
         */
        if (chain.size < 3) {
            return chain.map {
                RevoluteJoint(immutableListOf(it))
            }.toImmutableList()
        }

        val elements = mutableListOf<DhChainElement>()

        /**
         * Iterate in strides of 3 links. If we find a wrist, add it and skip over those three
         * links. If not, add the first link and skip over it.
         */
        var i = 0
        while (i < chain.size) {
            if (i + 3 > chain.size) {
                elements.add(RevoluteJoint(immutableListOf(chain[i])))
                i++
            } else {
                val possibleWrist = chain.subList(i, i + 3)

                if (wristIdentifier.isSphericalWrist(possibleWrist).isEmpty()) {
                    elements.add(SphericalWrist(possibleWrist))
                    i += 3
                } else {
                    elements.add(RevoluteJoint(immutableListOf(chain[i])))
                    i++
                }
            }
        }

        return elements.toImmutableList()
    }
}
