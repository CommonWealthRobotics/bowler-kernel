/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerkernel.kinematics.solvers.dhparam

import Jama.Matrix
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.toFrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation

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
    fun center(target: FrameTransformation): Matrix {
        val wristCenter = Matrix(3, 1)
        val boneLength = params[1].r + params[2].d
        val translation = target.translation
        val rotation = target.rotation

        wristCenter[0, 0] = translation[0, 0] - boneLength * rotation[0, 2]
        wristCenter[1, 0] = translation[1, 0] - boneLength * rotation[1, 2]
        wristCenter[2, 0] = translation[2, 0] - boneLength * rotation[2, 2]

        return wristCenter
    }

    /**
     * Calculate the position of the wrist's center in its home position.
     *
     * @param priorLinks The links that come before the wrist in the chain.
     * @return A 3x1 position matrix.
     */
    fun centerHomed(priorLinks: ImmutableList<DhParam>) =
        (priorLinks.toFrameTransformation() *
            params.subList(0, 2).toFrameTransformation()).translation
}
