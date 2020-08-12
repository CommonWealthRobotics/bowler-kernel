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
package com.commonwealthrobotics.bowlerkernel.gitfs

import com.commonwealthrobotics.proto.gitfs.FileSpec
import com.commonwealthrobotics.proto.gitfs.ProjectSpec
import java.io.File

/**
 * Resolves the dependencies needed by scripts.
 */
interface DependencyResolver {

    /**
     * Resolve a local [File] from a [FileSpec]. Dependency management works as follows:
     * - Scripts may import other scripts in their project. These dependencies are resolved to the relevant files in
     * the project.
     * - Scripts may import scripts in other Bowler libraries. By default, [GitFS] will be used to clone the library the
     * script is contained in and the script will be resolved to the local file within that library. However, when
     * developing another Bowler library, it may be useful to resolve scripts located in that library to their local
     * files instead of to the files on the remote. In this case, the library should be added as a dev using [addDev].
     * If a script is located inside a project that is dev'd, then that script will be resolved to the local file inside
     * that dev.
     */
    fun resolve(fileSpec: FileSpec): File

    /**
     * Add a dev to this resolver. See the [resolve] docs for more information.
     *
     * @param dev The project to dev.
     */
    fun addDev(dev: ProjectSpec)

    /**
     * Add devs to this resolver. See the [resolve] docs for more information.
     *
     * @param devs The projects to dev.
     */
    fun addDevs(devs: List<ProjectSpec>)
}
