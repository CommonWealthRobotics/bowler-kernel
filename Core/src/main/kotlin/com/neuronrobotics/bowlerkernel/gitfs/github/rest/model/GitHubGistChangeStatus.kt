package com.neuronrobotics.bowlerkernel.gitfs.github.rest.model

data class GitHubGistChangeStatus(
    val deletions: Int,
    val additions: Int,
    val total: Int
)
