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
package com.neuronrobotics.bowlerkernel.kinematics.limb.link

import arrow.core.Either
import arrow.core.extensions.either.monad.binding
import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.LinkConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.LinkScriptData
import com.neuronrobotics.bowlerkernel.kinematics.motion.InertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.createInstance
import com.neuronrobotics.bowlerkernel.scripting.factory.GitScriptFactory
import javax.inject.Inject

class DefaultLinkFactory
@Inject constructor(
    private val scriptFactory: GitScriptFactory,
    private val klaxon: Klaxon = Klaxon()
) : LinkFactory {

    override fun createLink(
        linkConfigurationData: LinkConfigurationData,
        linkScriptData: LinkScriptData
    ): Either<String, Link> = binding {
        val (estimator) = linkScriptData.inertialStateEstimator
            .createInstance<InertialStateEstimator>(scriptFactory, klaxon)

        DefaultLink(
            linkConfigurationData.type,
            linkConfigurationData.dhParamData.toDhParam(),
            estimator
        )
    }
}
