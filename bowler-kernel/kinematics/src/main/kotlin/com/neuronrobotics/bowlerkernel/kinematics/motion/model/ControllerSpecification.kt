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
import arrow.core.Try
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.Klaxon
import com.google.common.base.Throwables
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.scripting.factory.GitScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.getInstanceFromGit

/**
 * A specification that can be resolved into a controller. Either a [GitFile] or a [ClassData] that
 * can be parsed into a class.
 */
data class ControllerSpecification(
    val gitFile: GitFile? = null,
    val fullClassData: ClassData? = null
) {

    init {
        require((gitFile == null) xor (fullClassData == null))
    }

    /**
     * @return This [ControllerSpecification] as an [Either].
     */
    fun toEither(): Either<GitFile, ClassData> =
        gitFile?.left() ?: (fullClassData as ClassData).right()

    /**
     * Looks up a class by name [ClassData.fullClassName] and parses it using [klaxon].
     *
     * @param klaxon The [Klaxon] instance to use to parse the class data.
     * @return The parsed class.
     */
    inline fun <reified T : Any> loadClass(klaxon: Klaxon): T {
        require(fullClassData != null)

        val clazz = Class.forName(fullClassData.fullClassName)
        require(T::class.java.isAssignableFrom(clazz))

        return klaxon.fromJsonObject(
            klaxon.parseJsonObject(fullClassData.data.reader()),
            clazz,
            clazz.kotlin
        ) as T
    }

    /**
     * Creates an instance from this whether this is a [gitFile] or a [fullClassData].
     *
     * @param scriptFactory The [GitScriptFactory] used if this is a [gitFile].
     * @param klaxon The [Klaxon] used if this is a [fullClassData].
     * @return The instance.
     */
    inline fun <reified T : Any> createInstance(
        scriptFactory: GitScriptFactory,
        klaxon: Klaxon
    ): Either<String, T> {
        return when (val either = toEither()) {
            is Either.Left -> scriptFactory.getInstanceFromGit<T>(either.a).unsafeRunSync()
            is Either.Right -> Try { loadClass<T>(klaxon) }.toEither {
                Throwables.getStackTraceAsString(it)
            }
        }
    }

    companion object {

        /**
         * Converts an [Either] into a [ControllerSpecification].
         */
        fun fromEither(either: Either<GitFile, ClassData>) = when (either) {
            is Either.Left -> ControllerSpecification(gitFile = either.a)
            is Either.Right -> ControllerSpecification(fullClassData = either.b)
        }

        fun fromGitFile(gitFile: GitFile) = ControllerSpecification(gitFile, null)

        fun fromClassData(classData: ClassData) = ControllerSpecification(null, classData)
    }
}
