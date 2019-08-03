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
package com.neuronrobotics.bowlerkernel.kinematics.factories

import arrow.core.Either
import arrow.core.Try
import com.beust.klaxon.Klaxon
import com.google.common.base.Throwables.getStackTraceAsString
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.ControllerSpecification
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.loadClass
import com.neuronrobotics.bowlerkernel.scripting.factory.GitScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.getInstanceFromGit

/**
 * Creates an instance from this.
 *
 * @param scriptFactory The [GitScriptFactory] used if this is a [Either.Left].
 * @param klaxon The [Klaxon] used if this is a [Either.Right].
 * @return The instance.
 */
inline fun <reified T : Any> ControllerSpecification.createInstance(
    scriptFactory: GitScriptFactory,
    klaxon: Klaxon
): Either<String, T> = when (this) {
    is Either.Left -> scriptFactory.getInstanceFromGit<T>(a).unsafeRunSync()
    is Either.Right -> Try { loadClass<T>(klaxon) }.toEither {
        getStackTraceAsString(it)
    }
}
