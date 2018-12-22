/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.gitfs.github.rest.model

@SuppressWarnings("ConstructorParameterNaming")
data class GitHubGistCommit(
    val url: String,
    val version: String,
    val user: GitHubUser,
    val change_status: GitHubGistChangeStatus,
    val committed_at: String
)
