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
package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.Either
import arrow.core.Try
import arrow.core.flatMap
import arrow.core.right
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.Script
import de.swirtz.ktsrunner.objectloader.KtsObjectLoader
import groovy.lang.Binding
import groovy.lang.GroovyShell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import kotlin.reflect.KClass

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
    private var kotlinScript: Script? = null

    /**
     * Runs the script on the current thread.
     *
     * If the language is Groovy, some star imports are added and two variables, `args` and
     * `injector`, are added.
     *
     * If the language is Kotlin, special code structure must be used. The script must return a
     * [KClass] which implements [Script]. This class will make an instance of it with
     * [Script.injector] and will then call [Script.runScript] with `args`.
     *
     * @param args The arguments to the script.
     * @return The result of the script.
     */
    override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> =
        when (language) {
            is ScriptLanguage.Groovy -> runBlocking {
                handleGroovy(this, scriptText, args).toEither().mapLeft { it.localizedMessage }
            }

            is ScriptLanguage.Kotlin -> runBlocking {
                handleKotlin(this, scriptText, args).toEither().mapLeft {
                    it.localizedMessage
                }.flatMap { it }
            }
        }

    private suspend fun handleGroovy(
        coroutineScope: CoroutineScope,
        scriptText: String,
        args: ImmutableList<Any?>
    ): Try<Any?> {
        val configuration = CompilerConfiguration().apply {
            addCompilationCustomizers(
                ImportCustomizer().addStarImports(*groovyStarImports)
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

            coroutineScope.async { script.run() }
        }.map {
            scriptThread = it
            scriptThread?.await()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun handleKotlin(
        coroutineScope: CoroutineScope,
        scriptText: String,
        args: ImmutableList<Any?>
    ): Try<Either<String, Any?>> {
        return Try {
            coroutineScope.async {
                val result = KtsObjectLoader().load<Any?>(scriptText)
                if (result is KClass<*>) {
                    val instance = this@DefaultScript.injector.getInstance(result.java)
                    if (instance is Script) {
                        // Add all of this script's extra modules to the script to run
                        instance.addToInjector(this@DefaultScript.getModules())
                        kotlinScript = instance
                        instance.startScript(args)
                    } else {
                        instance.right()
                    }
                } else {
                    result.right()
                }
            }
        }.map {
            scriptThread = it
            scriptThread?.await() as Either<String, Any?>
        }
    }

    override fun stopScript() {
        scriptThread?.cancel()
        kotlinScript?.stopAndCleanUp()
    }

    companion object {
        private val groovyStarImports = listOf(
            "java.util",
            "java.io",
            "java.nio.file",
            "arrow.core",
            "eu.mihosoft.vrl.v3d",
            "eu.mihosoft.vrl.v3d.svg",
            "eu.mihosoft.vrl.v3d.samples",
            "eu.mihosoft.vrl.v3d.parametrics",
            "eu.mihosoft.vrl.v3d.Transform",
            "com.neuronrobotics.imageprovider",
            "com.neuronrobotics.sdk.addons.kinematics.xml",
            "com.neuronrobotics.sdk.addons.kinematics",
            "com.neuronrobotics.sdk.dyio.peripherals",
            "com.neuronrobotics.sdk.dyio",
            "com.neuronrobotics.sdk.common",
            "com.neuronrobotics.sdk.ui",
            "com.neuronrobotics.sdk.util",
            "com.neuronrobotics.sdk.serial",
            "com.neuronrobotics.sdk.addons.kinematics",
            "com.neuronrobotics.sdk.addons.kinematics.math",
            "com.neuronrobotics.sdk.addons.kinematics.gui",
            "com.neuronrobotics.sdk.config",
            "com.neuronrobotics.bowlerkernel",
            "com.neuronrobotics.bowlerstudio",
            "com.neuronrobotics.bowlerstudio.scripting",
            "com.neuronrobotics.bowlerstudio.physics",
            "com.neuronrobotics.bowlerstudio.vitamins",
            "com.neuronrobotics.bowlerstudio.creature"
        ).toTypedArray()
    }
}
