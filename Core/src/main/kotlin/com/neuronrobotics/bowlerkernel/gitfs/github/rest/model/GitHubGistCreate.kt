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
