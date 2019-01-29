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
package com.neuronrobotics.bowlerkernel.kinematics.limb

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbData

interface LimbFactory {

    /**
     * Creates a new limb from the [limbData].
     *
     * @param limbData The data to construct the limb from.
     * @return A new limb.
     */
    fun createLimb(limbData: LimbData): Either<LimbCreationError, Limb>
}
