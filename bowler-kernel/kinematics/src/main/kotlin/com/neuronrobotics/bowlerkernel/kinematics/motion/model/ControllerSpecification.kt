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
package com.neuronrobotics.bowlerkernel.kinematics.motion.model

import arrow.core.Either
import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.gitfs.decoder
import com.neuronrobotics.bowlerkernel.gitfs.encoder
import helios.instances.decoder
import helios.instances.encoder

/**
 * A specification that can be resolved into a controller. Either a [GitFile] or a [ClassData] that
 * can be parsed into a class.
 */
typealias ControllerSpecification = Either<GitFile, ClassData>

fun controllerSpecificationEncoder() =
    ControllerSpecification.encoder(GitFile.encoder(), ClassData.encoder())

fun controllerSpecificationDecoder() =
    ControllerSpecification.decoder(GitFile.decoder(), ClassData.decoder())

/**
 * Looks up a class by name [ClassData.fullClassName] and parses it using [klaxon].
 *
 * @param klaxon The [Klaxon] instance to use to parse the class data.
 * @return The parsed class.
 */
inline fun <reified T : Any> ControllerSpecification.loadClass(klaxon: Klaxon): T {
    require(isRight()) {
        "This ControllerSpecification must be Either.Right. Was:\n$this"
    }

    this as Either.Right

    val clazz = Class.forName(b.fullClassName)
    require(T::class.java.isAssignableFrom(clazz)) {
        "${T::class.java.name} is not assignable from ${clazz.name}."
    }

    return klaxon.fromJsonObject(
        klaxon.parseJsonObject(b.data.replace("\\\"", "\"").reader()),
        clazz,
        clazz.kotlin
    ) as T
}
