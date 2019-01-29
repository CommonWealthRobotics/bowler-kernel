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
import arrow.core.Try
import arrow.core.flatMap
import com.google.inject.assistedinject.Assisted
import com.neuronrobotics.bowlerkernel.scripting.DefaultScript
import com.neuronrobotics.bowlerkernel.scripting.parser.ScriptLanguageParser
import org.kohsuke.github.GitHub
import javax.inject.Inject

class DefaultGistScriptFactory
@Inject internal constructor(
    @Assisted private val gitHub: GitHub,
    private val scriptLanguageParser: ScriptLanguageParser
) : GistScriptFactory {

    /**
     * Creates a [DefaultScript] from a gist.
     *
     * @param gistId The gist id.
     * @param filename The file name in the gist.
     * @return A [DefaultScript] on success, a [String] on error.
     */
    override fun createScriptFromGist(
        gistId: String,
        filename: String
    ): Either<String, DefaultScript> =
        Try {
            val file = gitHub.getGist(gistId).files.entries.first { it.key == filename }.value
            val language = scriptLanguageParser.parse(file.language)
            language.map {
                DefaultScript(
                    it,
                    file.content
                )
            }
        }.toEither { it.localizedMessage }.flatMap { it }

    interface Factory {
        fun create(gitHub: GitHub): DefaultGistScriptFactory
    }
}
