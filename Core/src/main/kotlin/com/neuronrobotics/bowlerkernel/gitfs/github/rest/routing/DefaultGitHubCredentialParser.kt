package com.neuronrobotics.bowlerkernel.gitfs.github.rest.routing

import java.nio.file.Path

class DefaultGitHubCredentialParser : GitHubCredentialParser {

    override fun parseCredentials(credentialFilePath: Path): Pair<String, String> {
        val credentials = credentialFilePath.toFile()
            .readText()
            .removePrefix("https://")
            .removeSuffix("@github.com")
            .split(":")

        return if (credentials.size != 2) {
            throw IllegalStateException(
                """
                |Parsed more than two credentials from file (contents omitted for security):
                |$credentialFilePath
                """.trimMargin()
            )
        } else {
            credentials[0] to credentials[1]
        }
    }
}
