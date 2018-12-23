/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.gitfs.github.rest.routing.GitHubAPI
import groovy.lang.Binding
import groovy.lang.GroovyShell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

class GistScript
internal constructor(
    private val gitHubAPI: GitHubAPI,
    private val scriptLanguageParser: ScriptLanguageParser,
    private val gistId: String,
    private val filename: String
) : Script {

    private var scriptThread: Deferred<Any?>? = null

    override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> =
        runBlocking {
            val content = gitHubAPI.getGist(gistId).map {
                it.files.entries.first { it.key == filename }.value
            }

            content.flatMap { file ->
                scriptLanguageParser.parse(file.language ?: "").let {
                    when (it) {
                        is ScriptLanguage.Groovy -> handleGroovy(file.content, args).right()
                        //TODO: Kotlin scripting https://github.com/s1monw1/KtsRunner
                        is ScriptLanguage.ParseError -> it.message.left()
                    }
                }
            }
        }

    private suspend fun CoroutineScope.handleGroovy(
        scriptText: String,
        args: ImmutableList<Any?>
    ): Any? {
        val configuration = CompilerConfiguration().apply {
            addCompilationCustomizers(
                ImportCustomizer().addStarImports(
                    "com.neuronrobotics.bowlerbuilder",
                    "com.neuronrobotics.bowlerbuilder.controller",
                    "com.neuronrobotics.bowlerbuilder.view.tab",
                    "com.neuronrobotics.kinematicschef"
                )
            )
        }

        val script = GroovyShell(
            Thread.currentThread().contextClassLoader,
            Binding().apply { setVariable("args", args) },
            configuration
        ).parse(scriptText)

        scriptThread = async { script.run() }
        return scriptThread?.await()
    }

    override fun stopScript() {
        scriptThread?.cancel()
    }

    class Factory(
        private val gitHubAPI: GitHubAPI,
        private val scriptLanguageParser: ScriptLanguageParser
    ) {
        fun createGistScript(gistId: String, filename: String) =
            GistScript(gitHubAPI, scriptLanguageParser, gistId, filename)
    }
}
