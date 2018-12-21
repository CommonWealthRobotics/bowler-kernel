/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.gitfs.github.rest.model

data class GitHubGistFork(
    val user: GitHubUser,
    val url: String,
    val id: String,
    val created_at: String,
    val updated_at: String
)
