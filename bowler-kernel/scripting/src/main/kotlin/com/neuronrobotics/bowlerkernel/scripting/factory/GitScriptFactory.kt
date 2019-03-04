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
package com.neuronrobotics.bowlerkernel.scripting.factory

import arrow.core.Either
import arrow.core.flatMap
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.Script
import org.octogonapus.guavautil.collections.emptyImmutableList

interface GitScriptFactory {

    /**
     * Creates a [Script] from a Git URL.
     *
     * @param gitUrl The Git url.
     * @param filename The file name (including extension).
     * @return A [Script] on success, a [String] on error.
     */
    fun createScriptFromGit(gitUrl: String, filename: String): Either<String, Script>
}

/**
 * Creates an instance of [T] by running the [Script] given by its Git [pullUrl] and [filename].
 *
 * @param pullUrl The Git url.
 * @param filename The file name (including extension).
 * @param args The arguments to the script.
 * @return The result of running the [Script] and casting its result to [T].
 */
inline fun <reified T> GitScriptFactory.getInstanceFromGit(
    pullUrl: String,
    filename: String,
    args: ImmutableList<Any?> = emptyImmutableList()
) =
    createScriptFromGit(pullUrl, filename).flatMap { script ->
        script.runScript(args).map { it as T }
    }
