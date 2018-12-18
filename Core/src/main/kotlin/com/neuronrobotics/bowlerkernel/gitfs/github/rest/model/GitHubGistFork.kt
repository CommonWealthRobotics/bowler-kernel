package com.neuronrobotics.bowlerkernel.gitfs.github.rest.model

data class GitHubGistFork(
    val user: GitHubUser,
    val url: String,
    val id: String,
    val created_at: String,
    val updated_at: String
)
