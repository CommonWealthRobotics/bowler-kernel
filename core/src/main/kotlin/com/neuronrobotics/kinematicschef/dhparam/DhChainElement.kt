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
package com.neuronrobotics.kinematicschef.dhparam

import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.util.emptyImmutableList
import com.neuronrobotics.kinematicschef.util.toImmutableList

interface DhChainElement {

    /**
     * The DH params that make up this chain element. Could be one (revolute) or multiple
     * (spherical wrist).
     */
    val params: ImmutableList<DhParam>
}

/**
 * Collects the [DhChainElement.params] in this collection into one list.
 */
fun Collection<DhChainElement>.toDhParamList(): ImmutableList<DhParam> =
    fold(emptyImmutableList()) { acc, element ->
        (acc + element.params).toImmutableList()
    }
