/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.dhparam

import com.google.common.collect.ImmutableList
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
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
     * Calculate the position of a spherical wrist's center given a target point and a desired
     * wrist orientation.
     *
     * @param target The target position of the end effector.
     */
    internal fun center(target: SimpleMatrix): TransformNR {
        val wristCenter = TransformNR()
        val boneLength = params[1].r + params[2].d

        wristCenter.x = target[0, 3] - boneLength * target[0, 2]
        wristCenter.y = target[1, 3] - boneLength * target[1, 2]
        wristCenter.z = target[2, 3] - boneLength * target[2, 2]

        return wristCenter
    }
}
