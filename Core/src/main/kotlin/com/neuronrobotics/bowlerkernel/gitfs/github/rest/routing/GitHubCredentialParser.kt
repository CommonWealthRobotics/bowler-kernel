package com.neuronrobotics.bowlerkernel.gitfs.github.rest.routing

import java.nio.file.Path

interface GitHubCredentialParser {

    fun parseCredentials(credentialFilePath: Path): Pair<String, String>
}
