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
