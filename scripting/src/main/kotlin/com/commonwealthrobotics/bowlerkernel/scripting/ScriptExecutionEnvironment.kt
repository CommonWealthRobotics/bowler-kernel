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

/**
 * This "execution environment" provides all the facilities a script needs to interact with the Bowler stack. Scripts
 * should not interact with any other part of the Bowler stack directly; all interaction should go through this
 * interface.
 */
interface ScriptExecutionEnvironment {

    /**
     * Adds a thread to the list of threads this script tracks.
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
}
