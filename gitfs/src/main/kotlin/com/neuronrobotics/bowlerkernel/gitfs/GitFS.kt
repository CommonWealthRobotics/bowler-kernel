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
package com.neuronrobotics.bowlerkernel.gitfs

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
     * Clones a repository and returns the files in it, excluding the `.git` files.
     *
     * @param gitUrl The `.git` URL to clone from, i.e.
     * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
     * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
     * @param branch The branch to checkout.
     * @return The files in the repository.
     */
    fun cloneRepoAndGetFiles(gitUrl: String, branch: String = "HEAD"): IO<Set<File>>

    /**
     * Forks a repository.
     *
     * @param gitUrl The `.git` URL to fork from, i.e.
     * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
     * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
     * @return The git URL of the fork.
     */
    fun forkRepo(gitUrl: String): IO<String>

    /**
     * Forks a repository and clones the fork.
     *
     * @param gitUrl The `.git` URL to fork from, i.e.
     * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
     * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
     * @param branch The branch to checkout.
     * @return The directory of the cloned repository.
     */
    fun forkAndCloneRepo(gitUrl: String, branch: String = "HEAD"): IO<File>

    /**
     * Forks a repository, clones the fork, and returns the files in it, excluding the `.git`
     * files.
     *
     * @param gitUrl The `.git` URL to fork from, i.e.
     * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
     * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
     * @param branch The branch to checkout.
     * @return The files in the repository.
     */
    fun forkAndCloneRepoAndGetFiles(
        gitUrl: String,
        branch: String = "HEAD"
    ): IO<Set<File>>

    /**
     * Returns whether the authenticated user has push access to the Git resource at [gitUrl].
     *
     * @param gitUrl
     */
    fun isOwner(gitUrl: String): IO<Boolean>

    /**
     * Clears the local Git cache.
     */
    fun deleteCache()
}
