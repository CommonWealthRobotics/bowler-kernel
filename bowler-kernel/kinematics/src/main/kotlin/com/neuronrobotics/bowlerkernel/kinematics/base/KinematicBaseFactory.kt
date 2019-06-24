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
package com.neuronrobotics.bowlerkernel.kinematics.base

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.kinematics.base.model.FullySpecifiedKinematicBaseData
import com.neuronrobotics.bowlerkernel.kinematics.base.model.PartiallySpecifiedKinematicBaseData
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.BodyController

interface KinematicBaseFactory {

    /**
     * Creates a new kinematic base from the [fullySpecifiedKinematicBaseData].
     *
     * @param fullySpecifiedKinematicBaseData The data to construct the base from.
     * @return A new kinematic base.
     */
    fun create(
        fullySpecifiedKinematicBaseData: FullySpecifiedKinematicBaseData
    ): Either<String, KinematicBase>

    /**
     * Creates a new kinematic base from the [partiallySpecifiedKinematicBaseData].
     *
     * @param partiallySpecifiedKinematicBaseData The data to construct the base from.
     * @param bodyController The body controller to use.
     * @return A new kinematic base.
     */
    fun create(
        partiallySpecifiedKinematicBaseData: PartiallySpecifiedKinematicBaseData,
        bodyController: BodyController
    ): Either<String, KinematicBase>
}
