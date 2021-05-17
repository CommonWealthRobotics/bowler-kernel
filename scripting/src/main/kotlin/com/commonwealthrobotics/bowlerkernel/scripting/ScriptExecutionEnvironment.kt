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
import com.commonwealthrobotics.proto.script_host.ScriptOutput
import java.io.PrintStream

/**
 * This "execution environment" provides all the facilities a script needs to interact with the Bowler stack. Scripts
 * should not interact with any other part of the Bowler stack directly; all interaction should go through this
 * interface.
 *
 * None of these functions are suspending because this interface may be used by API consumers from any JVM language.
 * This interface should be as simple as possible to use.
 *
 * There are some injected variables in Groovy scripts:
 * 1. `args`: The argument passes to the script.
 * 2. `scriptEnvironment`: The script environment map.
 * 3. `scriptExecutionEnvironment`, `bowler`: The script-local execution environment (i.e. an instance of this
 * interface).
 *
 * TODO: Need to add a way for the script to get its output directory. Any files written into this directory will be
 *  synced to the client when the script finishes using [ScriptOutput].
 */
interface ScriptExecutionEnvironment {

    /**
     * Adds a thread to the list of threads this script tracks. All threads a script starts must be tracked using this
     * method. Starting a thread but not passing it to this method is undefined behavior.
     *
     * @param thread The thread to add.
     */
    fun addThread(thread: Thread)

    /**
     * Resolves and runs the script specified by the [fileSpec]. Scripts started this way inherit the
     * [DependencyResolver] of their parent script, meaning that and devs given when starting the parent script will be
     * inherited by all scripts within the execution tree.
     *
     * @param fileSpec The script to run.
     * @param scriptEnvironment The environment to pass to the script.
     * @param args The arguments to pass to the script.
     * @return A started script.
     */
    fun startChildScript(fileSpec: FileSpec, scriptEnvironment: Map<String, String>, args: List<Any?>): Script

    /**
     * An analog to System.stdout that scripts should use if they want to print back to the client.
     */
    val out: PrintStream

    /**
     * An analog to System.stderr that scripts should use if they want to print back to the client.
     */
    val err: PrintStream
}
