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

internal interface ChainIdentifier {

    /**
     * Identify the input [chain] by categorizing its DH params into chain elements.
     *
     * @param chain The DH chain to identify.
     * @return The elements that form the chain.
     */
    fun identifyChain(chain: ImmutableList<DhParam>): ImmutableList<DhChainElement>
}
