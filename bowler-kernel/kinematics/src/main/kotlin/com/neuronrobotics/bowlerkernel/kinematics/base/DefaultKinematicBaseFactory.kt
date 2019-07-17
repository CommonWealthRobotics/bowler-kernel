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
import arrow.core.extensions.either.monad.binding
import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseScriptData
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.BodyController
import com.neuronrobotics.bowlerkernel.kinematics.limb.LimbFactory
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.createInstance
import com.neuronrobotics.bowlerkernel.scripting.factory.GitScriptFactory
import javax.inject.Inject

class DefaultKinematicBaseFactory
@Inject constructor(
    private val scriptFactory: GitScriptFactory,
    private val limbFactory: LimbFactory,
    private val klaxon: Klaxon = Klaxon().converter(FrameTransformation)
) : KinematicBaseFactory {

    override fun create(
        kinematicBaseConfigurationData: KinematicBaseConfigurationData,
        kinematicBaseScriptData: KinematicBaseScriptData
    ): Either<String, KinematicBase> = binding {
        val (bodyController) = kinematicBaseScriptData.bodyController
            .createInstance<BodyController>(scriptFactory, klaxon)

        TODO()

        // val limbs = kinematicBaseConfigurationData.limbConfigurations
        //     .zip(kinematicBaseScriptData.limbScripts)
        //     .map { limbFactory.createLimb(it.first, it.second).bind() }
        //     .toImmutableList()
        //
        // val limbTransforms = limbs.map { it.id }
        //     .zip(kinematicBaseConfigurationData.limbTransforms)
        //     .toImmutableMap()
        //
        // DefaultKinematicBase(
        //     SimpleKinematicBaseId(kinematicBaseConfigurationData.id),
        //     limbs,
        //     limbTransforms,
        //     bodyController
        // )
    }
}
