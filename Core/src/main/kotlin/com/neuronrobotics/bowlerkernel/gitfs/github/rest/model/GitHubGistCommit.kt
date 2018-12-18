package com.neuronrobotics.bowlerkernel.gitfs.github.rest.model

data class GitHubGistCommit(
    val url: String,
    val version: String,
    val user: GitHubUser,
    val change_status: GitHubGistChangeStatus,
    val committed_at: String
)
