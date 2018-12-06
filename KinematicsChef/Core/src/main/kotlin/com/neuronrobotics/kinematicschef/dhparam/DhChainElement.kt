/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.dhparam

import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.util.emptyImmutableList
import com.neuronrobotics.kinematicschef.util.toImmutableList

internal interface DhChainElement {

    /**
     * The DH params that make up this chain element. Could be one (revolute) or multiple
     * (spherical wrist).
     */
    val params: ImmutableList<DhParam>
}

/**
 * Collects the [DhChainElement.params] in this collection into one list.
 */
internal fun Collection<DhChainElement>.toDhParamList(): ImmutableList<DhParam> =
    fold(emptyImmutableList()) { acc, element ->
        (acc + element.params).toImmutableList()
    }
