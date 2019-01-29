/*
 * This file is part of kinematics-chef.
 *
 * kinematics-chef is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kinematics-chef is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with kinematics-chef.  If not, see <https://www.gnu.org/licenses/>.
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

class DefaultChainIdentifier
@Inject internal constructor(
    private val wristIdentifier: WristIdentifier
) : ChainIdentifier {

    /**
     * Identify the input [chain] by categorizing its DH params into chain elements.
     *
     * @param chain The DH chain to identify.
     * @return The elements that form the chain.
     */
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

    companion object {
        fun create(wristIdentifier: WristIdentifier) = DefaultChainIdentifier(wristIdentifier)
    }
}
