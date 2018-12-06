/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.dhparam

import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.util.getTranslation
import org.ejml.simple.SimpleMatrix

/**
 * A spherical wrist. Has three DH params.
 */
internal data class SphericalWrist(override val params: ImmutableList<DhParam>) : DhChainElement {
    init {
        require(params.size == 3) {
            "A spherical wrist must have exactly 3 DH params, got ${params.size}."
        }
    }

    /**
     * Calculate the position of this wrist's center given a target point and a desired orientation.
     *
     * @param target The target position of the end effector.
     * @return A 3x1 position matrix.
     */
    internal fun center(target: SimpleMatrix): SimpleMatrix {
        val wristCenter = SimpleMatrix(3, 1)
        val boneLength = params[1].r + params[2].d

        wristCenter[0, 0] = target[0, 3] - boneLength * target[0, 2]
        wristCenter[1, 0] = target[1, 3] - boneLength * target[1, 2]
        wristCenter[2, 0] = target[2, 3] - boneLength * target[2, 2]

        return wristCenter
    }

    /**
     * Calculate the position of the wrist's center in its home position.
     *
     * @param priorLinks The links that come before the wrist in the chain.
     * @return A 3x1 position matrix.
     */
    internal fun centerHomed(priorLinks: ImmutableList<DhParam>) =
        priorLinks.toFrameTransformation().mult(
            params.subList(0, 2).toFrameTransformation()
        ).getTranslation()
}
