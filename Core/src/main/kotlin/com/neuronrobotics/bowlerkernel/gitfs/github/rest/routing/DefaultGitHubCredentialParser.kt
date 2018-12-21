/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
