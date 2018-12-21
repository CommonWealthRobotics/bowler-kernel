/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.gitfs.github.rest.model

/**
 * @param id The gist id.
 * @param description A descriptive name for the gist.
 * @param files A map of filename to file content.
 */
data class GitHubGistEdit(
    val id: String,
    val description: String,
    val files: Map<String, GitHubFileEdit>
) {
    init {
        validateFilenames(files)
    }
}
