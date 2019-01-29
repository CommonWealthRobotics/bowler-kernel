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
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseData
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.BodyController
import com.neuronrobotics.bowlerkernel.kinematics.limb.LimbFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.GitScriptFactory
import org.octogonapus.guavautil.collections.emptyImmutableList
import org.octogonapus.guavautil.collections.toImmutableList
import org.octogonapus.guavautil.collections.toImmutableMap
import javax.inject.Inject

class DefaultKinematicBaseFactory
@Inject constructor(
    private val scriptFactory: GitScriptFactory,
    private val limbFactory: LimbFactory
) : KinematicBaseFactory {

    override fun create(
        kinematicBaseData: KinematicBaseData
    ): Either<KinematicBaseCreationError, KinematicBase> {
        val limbs = kinematicBaseData.limbs.map {
            limbFactory.createLimb(it).fold({ return it.left() }, { it })
        }.toImmutableList()

        val limbTransforms = limbs.map { it.id }
            .zip(kinematicBaseData.limbTransforms)
            .toImmutableMap()

        val bodyController = getInstanceFromGist<BodyController>(
            kinematicBaseData.bodyControllerGistId,
            kinematicBaseData.bodyControllerFilename
        ).fold({ return it.left() }, { it })

        return DefaultKinematicBase(
            kinematicBaseData.id,
            limbs,
            limbTransforms,
            bodyController
        ).right()
    }

    private inline fun <reified T> getInstanceFromGist(gistId: String, filename: String) =
        scriptFactory.createScriptFromGit(gistId, filename).flatMap { script ->
            script.runScript(emptyImmutableList()).map { it as T }
        }
}
