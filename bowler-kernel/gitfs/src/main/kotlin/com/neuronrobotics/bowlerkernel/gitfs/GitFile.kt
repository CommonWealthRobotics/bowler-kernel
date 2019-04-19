package com.neuronrobotics.bowlerkernel.gitfs

/**
 * Represents a file in Git.
 *
 * @param gitUrl The `.git` URL the repository could be cloned from, i.e.
 * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
 * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
 * @param filename The name of the file in the repo (including extension).
 */
data class GitFile(
    val gitUrl: String,
    val filename: String
)
