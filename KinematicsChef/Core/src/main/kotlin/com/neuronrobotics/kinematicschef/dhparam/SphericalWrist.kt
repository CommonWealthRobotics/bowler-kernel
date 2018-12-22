/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.dhparam

import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.util.getRotation
import com.neuronrobotics.kinematicschef.util.getTranslation
import org.ejml.simple.SimpleMatrix

/**
 * A spherical wrist. Has three DH params.
 */
data class SphericalWrist(override val params: ImmutableList<DhParam>) : DhChainElement {
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
    fun center(target: SimpleMatrix): SimpleMatrix {
        require(target.numRows() == 4)
        require(target.numCols() == 4)

        val wristCenter = SimpleMatrix(3, 1)
        val boneLength = params[1].r + params[2].d
        val translation = target.getTranslation()
        val rotation = target.getRotation()

        wristCenter[0] = translation[0] - boneLength * rotation[0, 2]
        wristCenter[1] = translation[1] - boneLength * rotation[1, 2]
        wristCenter[2] = translation[2] - boneLength * rotation[2, 2]

        return wristCenter
    }

    /**
     * Calculate the position of the wrist's center in its home position.
     *
     * @param priorLinks The links that come before the wrist in the chain.
     * @return A 3x1 position matrix.
     */
    fun centerHomed(priorLinks: ImmutableList<DhParam>) =
        priorLinks.toFrameTransformation().mult(
            params.subList(0, 2).toFrameTransformation()
        ).getTranslation()
}
