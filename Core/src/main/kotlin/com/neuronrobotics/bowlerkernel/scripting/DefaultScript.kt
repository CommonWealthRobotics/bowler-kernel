/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.Either
import arrow.core.Try
import com.google.common.collect.ImmutableList
import groovy.lang.Binding
import groovy.lang.GroovyShell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * A meta-script which can compile and run any known [ScriptLanguage].
 *
 * Notes for [ScriptLanguage.Groovy]:
 * Passes the `args` as a variable named `args` and passes the [Script.injector] as a variable
 * named `injector`.
 */
class DefaultScript
internal constructor(
    private val language: ScriptLanguage,
    private val scriptText: String
) : Script() {

    private var scriptThread: Deferred<Any?>? = null

    override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> =
        when (language) {
            //TODO: Kotlin scripting https://github.com/s1monw1/KtsRunner
            is ScriptLanguage.Groovy -> runBlocking {
                handleGroovy(scriptText, args).toEither().mapLeft { it.localizedMessage }
            }
        }

    private suspend fun CoroutineScope.handleGroovy(
        scriptText: String,
        args: ImmutableList<Any?>
    ): Try<Any?> {
        val configuration = CompilerConfiguration().apply {
            addCompilationCustomizers(
                ImportCustomizer().addStarImports(
                    "com.neuronrobotics.bowlerbuilder",
                    "com.neuronrobotics.bowlerbuilder.controller",
                    "com.neuronrobotics.bowlerbuilder.view.tab",
                    "com.neuronrobotics.kinematicschef",
                    "eu.mihosoft.vrl.v3d"
                )
            )
        }

        return Try {
            val script = GroovyShell(
                Thread.currentThread().contextClassLoader,
                Binding().apply {
                    setVariable("args", args)
                    setVariable("injector", injector)
                },
                configuration
            ).parse(scriptText)

            async { script.run() }
        }.map {
            scriptThread = it
            scriptThread?.await()
        }
    }

    override fun stopScript() {
        scriptThread?.cancel()
    }
}
