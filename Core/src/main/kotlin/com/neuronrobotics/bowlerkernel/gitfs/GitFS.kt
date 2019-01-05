/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.gitfs

import arrow.core.Try
import com.google.common.collect.ImmutableList
import java.io.File

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
    fun cloneRepo(gitUrl: String, branch: String = "HEAD"): Try<File>

    /**
     * Clones a repository and returns the files in it, excluding the `.git` files.
     *
     * @param gitUrl The `.git` URL to clone from, i.e.
     * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
     * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
     * @param branch The branch to checkout.
     * @return The files in the repository.
     */
    fun cloneRepoAndGetFiles(gitUrl: String, branch: String = "HEAD"): Try<ImmutableList<File>>

    /**
     * Forks a repository.
     *
     * @param gitUrl The `.git` URL to fork from, i.e.
     * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
     * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
     * @return The git URL of the fork.
     */
    fun forkRepo(gitUrl: String): Try<String>

    /**
     * Forks a repository and clones the fork.
     *
     * @param gitUrl The `.git` URL to fork from, i.e.
     * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
     * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
     * @param branch The branch to checkout.
     * @return The directory of the cloned repository.
     */
    fun forkAndCloneRepo(gitUrl: String, branch: String = "HEAD"): Try<File>

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
    ): Try<ImmutableList<File>>

    /**
     * Returns whether the authenticated user has push access to the Git resource at [gitUrl].
     *
     * @param gitUrl
     */
    fun isOwner(gitUrl: String): Try<Boolean>
}
