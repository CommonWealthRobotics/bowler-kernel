package com.neuronrobotics.bowlerkernel.gitfs.github.rest.model

data class GitHubFile(
    val filename: String,
    val type: String,
    val language: String?,
    val raw_url: String,
    val size: Int
)

data class GitHubFileEdit(
    val filename: String?,
    val content: String
)
