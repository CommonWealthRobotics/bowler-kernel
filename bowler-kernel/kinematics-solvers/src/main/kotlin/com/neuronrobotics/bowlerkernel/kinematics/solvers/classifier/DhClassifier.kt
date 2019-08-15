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
package com.neuronrobotics.bowlerkernel.kinematics.solvers.classifier

import arrow.core.Either
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.solvers.dhparam.DhChainElement
import com.neuronrobotics.bowlerkernel.kinematics.solvers.dhparam.SphericalWrist
import com.neuronrobotics.bowlerkernel.kinematics.solvers.eulerangle.EulerAngle

/**
 * Derives Euler angles for a [SphericalWrist].
 */
interface DhClassifier {

    /**
     * Determine the Euler angles for a [SphericalWrist].
     *
     * @param wrist The wrist to classify. The thetas must be specified as offsets.
     * @return The Euler angles or an error.
     */
    fun deriveEulerAngles(wrist: SphericalWrist): Either<String, EulerAngle>

    /**
     * Determine the Euler angles for a [SphericalWrist].
     *
     * @param wrist The wrist to classify. The thetas must be specified as offsets.
     * @param priorChain The chain elements before the wrist.
     * @param followingChain The chain elements after the wrist.
     * @return The Euler angles or an error.
     */
    fun deriveEulerAngles(
        wrist: SphericalWrist,
        priorChain: ImmutableList<DhChainElement>,
        followingChain: ImmutableList<DhChainElement>
    ): Either<String, EulerAngle>
}
