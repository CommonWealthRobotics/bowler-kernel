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
