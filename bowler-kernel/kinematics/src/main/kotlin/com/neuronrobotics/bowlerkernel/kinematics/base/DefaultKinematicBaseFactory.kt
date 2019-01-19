/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.kinematics.base

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseData
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.BodyController
import com.neuronrobotics.bowlerkernel.kinematics.limb.LimbFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.GistScriptFactory
import org.octogonapus.guavautil.collections.emptyImmutableList
import org.octogonapus.guavautil.collections.toImmutableList
import org.octogonapus.guavautil.collections.toImmutableMap
import javax.inject.Inject

class DefaultKinematicBaseFactory
@Inject constructor(
    private val scriptFactory: GistScriptFactory,
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
        scriptFactory.createScriptFromGist(gistId, filename).flatMap { script ->
            script.runScript(emptyImmutableList()).map { it as T }
        }
}
