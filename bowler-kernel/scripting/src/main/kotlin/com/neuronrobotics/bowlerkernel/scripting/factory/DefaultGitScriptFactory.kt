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
import com.google.inject.assistedinject.Assisted
import com.neuronrobotics.bowlerkernel.gitfs.GitFS
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.hardware.Script
import com.neuronrobotics.bowlerkernel.scripting.DefaultScript
import com.neuronrobotics.bowlerkernel.scripting.parser.ScriptLanguageParser
import javax.inject.Inject

class DefaultGitScriptFactory
@Inject internal constructor(
    @Assisted private val gitFS: GitFS,
    private val scriptLanguageParser: ScriptLanguageParser
) : GitScriptFactory {

    /**
     * Creates a [DefaultScript] from a gist.
     *
     * @param gitFile The Git file.
     * @return A [DefaultScript] on success, a [String] on error.
     */
    override fun createScriptFromGit(gitFile: GitFile): Either<String, Script> =
        gitFS.cloneRepoAndGetFiles(gitFile.gitUrl).map {
            val file = it.first { it.name == gitFile.filename }
            scriptLanguageParser.parse(file.extension).map { DefaultScript(it, file.readText()) }
        }.attempt().unsafeRunSync().mapLeft { it.localizedMessage }.flatMap { it }

    interface Factory {
        fun create(gitFS: GitFS): DefaultGitScriptFactory
    }
}
