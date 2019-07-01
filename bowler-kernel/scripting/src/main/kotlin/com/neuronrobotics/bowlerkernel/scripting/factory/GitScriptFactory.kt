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

import arrow.effects.IO
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.hardware.Script
import org.octogonapus.ktguava.collections.emptyImmutableList

interface GitScriptFactory {

    /**
     * Creates a [Script] from a Git file.
     *
     * @param gitFile The Git file.
     * @return A [Script] on success, a [String] on error.
     */
    fun createScriptFromGit(gitFile: GitFile): IO<Script>
}

/**
 * Creates an instance of [T] by running the [Script].
 *
 * @param gitFile The Git file.
 * @param args The arguments to the script.
 * @return The result of running the [Script] and casting its result to [T].
 */
inline fun <reified T> GitScriptFactory.getInstanceFromGit(
    gitFile: GitFile,
    args: ImmutableList<Any?> = emptyImmutableList()
) = createScriptFromGit(gitFile).map { script ->
    script.startScript(args).map { it as T }.also {
        script.stopAndCleanUp()
    }
}
