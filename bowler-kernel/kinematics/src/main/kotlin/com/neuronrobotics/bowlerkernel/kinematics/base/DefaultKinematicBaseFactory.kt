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
import arrow.core.left
import arrow.core.right
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.SimpleKinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.base.model.FullySpecifiedKinematicBaseData
import com.neuronrobotics.bowlerkernel.kinematics.base.model.PartiallySpecifiedKinematicBaseData
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.BodyController
import com.neuronrobotics.bowlerkernel.kinematics.limb.LimbFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.GitScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.getInstanceFromGit
import org.octogonapus.ktguava.collections.toImmutableList
import org.octogonapus.ktguava.collections.toImmutableMap
import javax.inject.Inject

class DefaultKinematicBaseFactory
@Inject constructor(
    private val scriptFactory: GitScriptFactory,
    private val limbFactory: LimbFactory
) : KinematicBaseFactory {

    override fun create(
        fullySpecifiedKinematicBaseData: FullySpecifiedKinematicBaseData
    ): Either<String, KinematicBase> {
        val bodyController = scriptFactory.getInstanceFromGit<BodyController>(
            fullySpecifiedKinematicBaseData.bodyController
        ).fold({ return it.left() }, { it })

        return create(fullySpecifiedKinematicBaseData.partialData, bodyController)
    }

    override fun create(
        partiallySpecifiedKinematicBaseData: PartiallySpecifiedKinematicBaseData,
        bodyController: BodyController
    ): Either<String, KinematicBase> {
        val limbs = partiallySpecifiedKinematicBaseData.limbs.map {
            limbFactory.createLimb(it).fold({ return it.left() }, { it })
        }.toImmutableList()

        val limbTransforms = limbs.map { it.id }
            .zip(partiallySpecifiedKinematicBaseData.limbTransforms)
            .toImmutableMap()

        return DefaultKinematicBase(
            SimpleKinematicBaseId(partiallySpecifiedKinematicBaseData.id),
            limbs,
            limbTransforms,
            bodyController
        ).right()
    }
}
