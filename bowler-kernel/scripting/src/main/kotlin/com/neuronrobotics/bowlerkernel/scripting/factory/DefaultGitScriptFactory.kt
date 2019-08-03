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
import com.google.inject.assistedinject.Assisted
import com.neuronrobotics.bowlerkernel.gitfs.GitFS
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.hardware.Script
import com.neuronrobotics.bowlerkernel.scripting.DefaultScript
import com.neuronrobotics.bowlerkernel.scripting.parser.ScriptLanguageParser
import javax.inject.Inject

class DefaultGitScriptFactory
@Inject constructor(
    @Assisted private val gitFS: GitFS,
    private val scriptLanguageParser: ScriptLanguageParser
) : GitScriptFactory {

    /**
     * Creates a [DefaultScript] from a gist.
     *
     * @param gitFile The Git file.
     * @return A [DefaultScript] on success, a [String] on error.
     */
    override fun createScriptFromGit(gitFile: GitFile): IO<Script> =
        gitFS.cloneRepoAndGetFiles(gitFile.gitUrl).flatMap { files ->
            val file = files.first { it.name == gitFile.filename }
            scriptLanguageParser.parse(file.extension).fold(
                { IO.raiseError<Script>(IllegalStateException(it)) },
                { IO.just(DefaultScript(it, file.readText())) }
            )
        }

    @Suppress("unused")
    interface Factory {
        fun create(gitFS: GitFS): DefaultGitScriptFactory
    }
}
