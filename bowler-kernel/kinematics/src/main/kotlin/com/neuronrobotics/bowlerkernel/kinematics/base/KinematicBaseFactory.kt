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
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseScriptData
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.LimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbScriptData
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation

interface KinematicBaseFactory {

    /**
     * Creates a new kinematic base.
     *
     * @param kinematicBaseConfigurationData The configuration data.
     * @param kinematicBaseScriptData The script data.
     * @param limbData The data for all the limbs attached to the base.
     * @param limbTransforms The base transforms for all the limbs attached to the base.
     * @return A new kinematic base.
     */
    fun create(
        kinematicBaseConfigurationData: KinematicBaseConfigurationData,
        kinematicBaseScriptData: KinematicBaseScriptData,
        limbData: List<Pair<LimbConfigurationData, LimbScriptData>>,
        limbTransforms: Map<LimbId, FrameTransformation>
    ): Either<String, KinematicBase>
}
