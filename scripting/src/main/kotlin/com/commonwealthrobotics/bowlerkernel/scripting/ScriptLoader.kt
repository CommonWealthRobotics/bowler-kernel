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
import com.commonwealthrobotics.proto.gitfs.ProjectSpec

interface ScriptLoader {

    /**
     * Resolves the script specified by the [fileSpec] and loads it into an instance of [Script]. Adds the [devs] for
     * dependency resolution. Passes the [scriptEnvironment] to the script.
     *
     * @param fileSpec The script to resolve and load.
     * @param devs Any dev'd dependencies.
     * @param scriptEnvironment The script environment.
     * @return A runnable script.
     */
    suspend fun resolveAndLoad(
        fileSpec: FileSpec,
        devs: List<ProjectSpec>,
        scriptEnvironment: Map<String, String>
    ): Script
}
