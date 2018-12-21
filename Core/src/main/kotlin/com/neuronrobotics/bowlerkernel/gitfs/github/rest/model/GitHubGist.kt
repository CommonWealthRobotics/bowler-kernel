/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.gitfs.github.rest.model

data class GitHubGist(
    val url: String,
    val forks_url: String,
    val commits_url: String,
    val id: String,
    val node_id: String,
    val git_pull_url: String,
    val git_push_url: String,
    val html_url: String,
    val files: Map<String, GitHubFile>,
    val public: Boolean,
    val created_at: String,
    val updated_at: String,
    val description: String?,
    val comments: Int,
    val user: GitHubUser?,
    val comments_url: String,
    val owner: GitHubUser,
    val truncated: Boolean
)
