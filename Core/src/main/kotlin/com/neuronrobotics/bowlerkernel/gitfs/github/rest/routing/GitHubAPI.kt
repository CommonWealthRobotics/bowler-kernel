/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.gitfs.github.rest.routing

import arrow.core.Either
import arrow.core.Option
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.gitfs.github.rest.model.GitHubError
import com.neuronrobotics.bowlerkernel.gitfs.github.rest.model.GitHubFileEdit
import com.neuronrobotics.bowlerkernel.gitfs.github.rest.model.GitHubGist
import com.neuronrobotics.bowlerkernel.gitfs.github.rest.model.GitHubGistCommit
import com.neuronrobotics.bowlerkernel.gitfs.github.rest.model.GitHubGistCreate
import com.neuronrobotics.bowlerkernel.gitfs.github.rest.model.GitHubGistEdit
import com.neuronrobotics.bowlerkernel.gitfs.github.rest.model.GitHubGistFork

interface GitHubAPI {

    /**
     * The currently authenticated user.
     */
    val currentUser: String

    /**
     * Request all the Gists for the current user.
     *
     * @return The gists or an error.
     */
    suspend fun getGists(): Either<GitHubError, ImmutableList<GitHubGist>>

    /**
     * Request all the starred Gists for the current user.
     *
     * @return The gists or an error.
     */
    suspend fun getStarredGists(): Either<GitHubError, ImmutableList<GitHubGist>>

    /**
     * Request a single gist by [gistId].
     *
     * @param gistId The gist id.
     * @return The gist or an error.
     */
    suspend fun getGist(gistId: String): Either<GitHubError, GitHubGist>

    /**
     * Request a revision of a single gist by [gistId] and [revisionSha].
     *
     * @param gistId The gist id.
     * @param revisionSha The SHA hash revision number.
     * @return The gist or an error.
     */
    suspend fun getGistRevision(
        gistId: String,
        revisionSha: String
    ): Either<GitHubError, GitHubGist>

    /**
     * Create a new gist under the current user.
     *
     * @param gistCreate The creation data.
     * @return The new gist or an error.
     */
    suspend fun createGist(gistCreate: GitHubGistCreate): Either<GitHubError, GitHubGist>

    /**
     * Update or delete a gist file and rename gist files. Files from the previous version of the
     * gist that aren't explicitly changed during an edit are unchanged.
     *
     * To delete a file, set [GitHubFileEdit.filename] to `null`.
     *
     * @param gistEdit The edit data.
     * @return An optional error.
     */
    suspend fun editGist(gistEdit: GitHubGistEdit): Option<GitHubError>

    /**
     * Request the commits for a gist by [gistId].
     *
     * @param gistId The gist id.
     * @return The commits or an error.
     */
    suspend fun getCommits(gistId: String): Either<GitHubError, ImmutableList<GitHubGistCommit>>

    /**
     * Star a gist by [gistId].
     *
     * @param gistId The gist id.
     * @return An optional error.
     */
    suspend fun star(gistId: String): Option<GitHubError>

    /**
     * Unstar a gist by [gistId].
     *
     * @param gistId The gist id.
     * @return An optional error.
     */
    suspend fun unstar(gistId: String): Option<GitHubError>

    /**
     * Check if a gist is starred.
     *
     * @param gistId The gist id.
     * @return Whether the gist is starred or an error.
     */
    suspend fun isStarred(gistId: String): Either<GitHubError, Boolean>

    /**
     * Fork a gist by [gistId].
     *
     * @param gistId The gist id.
     * @return The fork of the gist or an error.
     */
    suspend fun fork(gistId: String): Either<GitHubError, GitHubGist>

    /**
     * Request the forks of a gist by [gistId].
     *
     * @param gistId The gist id.
     * @return The forks or an error.
     */
    suspend fun getForks(gistId: String): Either<GitHubError, ImmutableList<GitHubGistFork>>

    /**
     * Delete a gist by [gistId].
     *
     * @param gistId The gist id.
     * @return An optional error.
     */
    suspend fun delete(gistId: String): Option<GitHubError>
}
