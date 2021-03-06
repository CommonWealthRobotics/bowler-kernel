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
 *
 * Important note: the kernel's dependency cache is just that (a cache). The kernel is free to evict cache entries at
 * any time that would not cause a running program to break. Don't store important changes in the cache.
 */
interface DependencyResolver {

    /**
     * Resolve a local [File] from a [FileSpec].
     *
     * Dependency resolution works as follows:
     * 1. Scripts may import scripts from the same project. These dependencies are resolved to the relevant files
     * in that project.
     *
     * 2. Scripts may import scripts in other Bowler libraries that are hosted on a Git remote.
     *
     *     a) If the script being imported is not dev'd, that script is resolved to the version of the script on the
     *     remote.
     *
     *     b) If the script being imported is dev'd, that script is resolved to the local (local to the client) version
     *     of that script.
     *
     * Patches work as follows. The local copy of the project will be made consistent with the patch. Any previously
     * applied patch is discarded and overwritten by the new patch.
     *
     * @throws IllegalArgumentException If [GitFS] throws.
     * @throws UnsupportedOperationException If [GitFS] throws.
     */
    suspend fun resolve(fileSpec: FileSpec): File

    /**
     * Add a dev to this resolver. See the [resolve] docs for more information.
     *
     * @param dev The project to dev.
     */
    suspend fun addDev(dev: ProjectSpec)

    /**
     * Add devs to this resolver. See the [resolve] docs for more information.
     *
     * @param devs The projects to dev.
     */
    suspend fun addDevs(devs: List<ProjectSpec>)
}
