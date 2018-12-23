/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.gitfs.github.rest.model

fun createMockGistFile(
    filename: String, content: String
): Pair<String, GitHubFile> {
    val language = filename.substringAfterLast(".")
    return filename to GitHubFile(
        filename = filename,
        type = language,
        language = language,
        raw_url = "",
        size = 0,
        truncated = false,
        content = content
    )
}

fun createMockGist(
    gistId: String,
    files: Map<String, GitHubFile>
) = GitHubGist(
    url = "",
    forks_url = "",
    commits_url = "",
    id = "",
    node_id = "",
    git_pull_url = "",
    git_push_url = "",
    html_url = "",
    files = files,
    public = true,
    created_at = "",
    updated_at = "",
    description = "",
    comments = 0,
    user = null,
    comments_url = "",
    owner = GitHubUser(
        login = "",
        id = 0,
        node_id = "",
        avatar_url = "",
        url = "",
        html_url = "",
        followers_url = "",
        following_url = "",
        gists_url = "",
        starred_url = "",
        subscriptions_url = "",
        organizations_url = "",
        repos_url = "",
        events_url = "",
        type = "",
        site_admin = false
    ),
    truncated = false
)
