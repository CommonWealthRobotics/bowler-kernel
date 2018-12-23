/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.gitfs.github.rest.model

@SuppressWarnings("ConstructorParameterNaming")
data class GitHubFile(
    val filename: String,
    val type: String,
    val language: String?,
    val raw_url: String,
    val size: Int,
    val truncated: Boolean, // TODO: These are not present if a user gets all gists together
    val content: String // TODO: These are not present if a user gets all gists together
)

data class GitHubFileEdit(
    val filename: String?,
    val content: String
)
