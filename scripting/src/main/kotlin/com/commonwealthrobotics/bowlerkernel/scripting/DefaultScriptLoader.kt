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
package com.commonwealthrobotics.bowlerkernel.scripting

import com.commonwealthrobotics.bowlerkernel.gitfs.DependencyResolver
import com.commonwealthrobotics.proto.gitfs.FileSpec
import com.commonwealthrobotics.proto.gitfs.ProjectSpec
import groovy.lang.Binding
import groovy.lang.GroovyShell
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import java.io.File

/**
 * @param dependencyResolver The [DependencyResolver] used to resolve script files.
 */
class DefaultScriptLoader(
    private val dependencyResolver: DependencyResolver
) : ScriptLoader {

    override fun resolveAndLoad(
        fileSpec: FileSpec,
        devs: List<ProjectSpec>,
        scriptEnvironment: Map<String, String>
    ): Script {
        val scriptFile = dependencyResolver.resolve(fileSpec)
        dependencyResolver.addDevs(devs)
        return DefaultScript(loadGroovyScript(scriptFile, scriptEnvironment), this)
    }

    /**
     * Loads a Groovy script and sets up the returned [Thread] for execution.
     *
     * @param file The local file containing the script.
     * @param scriptEnvironment The environment map that will be given to the script every time it is run.
     * @return The closure that will be given the script's args to run the script. This closure uses the contextual
     * class loader from the thread it is run on.
     */
    private fun loadGroovyScript(file: File, scriptEnvironment: Map<String, String>): ScriptClosure {
        val compilerConfiguration = CompilerConfiguration().apply {
            addCompilationCustomizers(
                @Suppress("SpreadOperator")
                ImportCustomizer().addStarImports(*groovyStarImports)
            )
        }

        return { args: List<Any?>, scriptExecutionEnvironment: ScriptExecutionEnvironment ->
            val script = GroovyShell(
                Thread.currentThread().contextClassLoader,
                Binding().apply {
                    setVariable("args", args)
                    setVariable("scriptEnvironment", scriptEnvironment)
                    setVariable("scriptExecutionEnvironment", scriptExecutionEnvironment)
                },
                compilerConfiguration
            ).parse(file.readText()) // TODO: Expose Charset

            script.run()
        }
    }

    companion object {

        private val groovyStarImports = listOf(
            "java.util",
            "java.io",
            "java.nio.file",
            "eu.mihosoft.vrl.v3d",
            "eu.mihosoft.vrl.v3d.svg",
            "eu.mihosoft.vrl.v3d.samples",
            "eu.mihosoft.vrl.v3d.parametrics",
            "eu.mihosoft.vrl.v3d.Transform",
            "com.commonwealthrobotics.bowlerkernel.scripting"
        ).toTypedArray()
    }
}
