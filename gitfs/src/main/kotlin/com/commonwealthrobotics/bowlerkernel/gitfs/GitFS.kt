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

import arrow.fx.IO
import java.io.File

/**
 * An interface to a Git-based file system, typically hosted remotely.
 */
interface GitFS {

    /**
     * Clones a repository to the local cache.
     *
     * @param gitUrl The `.git` URL to clone from, i.e.
     * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
     * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
     * @param branch The branch to checkout.
     * @return The directory of the cloned repository.
     */
    fun cloneRepo(gitUrl: String, branch: String = "HEAD"): IO<File>

    /**
     * Gets the files in the repo, excluding the files inside `.git`.
     *
     * @param repoDir The directory of the repository root.
     * @return The files in the repository.
     */
    fun getFilesInRepo(repoDir: File): IO<Set<File>>

    /**
     * Clears the local Git cache.
     */
    fun deleteCache(): IO<Unit>
}
