/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.gitfs.github.rest.model

/**
 * @param files A map of filename to file content.
 * @param description A descriptive name for the gist.
 * @param public Whether the gist is public.
 */
data class GitHubGistCreate(
    val files: Map<String, String>,
    val description: String,
    val public: Boolean
) {
    init {
        validateFilenames(files)
    }
}
