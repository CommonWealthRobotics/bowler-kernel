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
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.kinematicschef.dhparam.DhChainElement

/**
 * Segments a list of [DhParam] into a list of [DhChainElement] by identifying abstract members of
 * the chain.
 */
interface ChainIdentifier {

    /**
     * Identify the input [chain] by categorizing its DH params into chain elements.
     *
     * @param chain The DH chain to identify.
     * @return The elements that form the chain.
     */
    fun identifyChain(chain: ImmutableList<DhParam>): ImmutableList<DhChainElement>
}
