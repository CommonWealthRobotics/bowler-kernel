/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.Either
import arrow.core.Try
import arrow.core.flatMap
import com.google.inject.assistedinject.Assisted
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
            language.map { DefaultScript(it, file.content) }
        }.toEither { it.localizedMessage }.flatMap { it }

    interface Factory {
        fun create(gitHub: GitHub): DefaultGistScriptFactory
    }
}
