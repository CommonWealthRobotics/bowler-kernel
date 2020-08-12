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

import com.commonwealthrobotics.proto.gitfs.FileSpec

interface ScriptExecutionEnvironment {

    /**
     * Adds a thread to the list of threads this script tracks.
     *
     * @param thread The thread to add.
     */
    fun addThread(thread: Thread)

    /**
     * Resolves and runs the script specified by the [fileSpec].
     *
     * @param fileSpec The script to run.
     * @param scriptEnvironment The environment to pass to the script.
     */
    fun resolveAndLoad(fileSpec: FileSpec, scriptEnvironment: Map<String, String>): Script
}