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

import java.io.File

/**
 * An interface to a Git-based file system, typically hosted remotely.
 *
 * Important note: the kernel's dependency cache is just that (a cache). The kernel is free to evict cache entries at
 * any time that would not cause a running program to break. Don't store important changes in the cache.
 */
interface GitFS {

    /**
     * Clones a repository to the local cache. The cached copy of the repository will be consistent with the remote
     * after this method returns. Namely, any local changes will be reset.
     *
     * @param gitUrl The `.git` URL to clone from, i.e.
     * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
     * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
     * @param revision The revision to checkout (a commit hash).
     * @return The directory of the cloned repository.
     * @throws IllegalArgumentException If the [gitUrl] is invalid.
     * @throws UnsupportedOperationException If the kernel is not allowed to authenticate to the [gitUrl].
     */
    suspend fun cloneRepo(gitUrl: String, revision: String): File

    /**
     * Gets the files in the repo, excluding the files inside `.git`.
     *
     * @param repoDir The directory of the repository root.
     * @return The files in the repository.
     */
    fun getFilesInRepo(repoDir: File): Set<File>

    /**
     * Clears the local Git cache.
     */
    fun deleteCache()
}
