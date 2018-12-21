/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.gitfs.github.rest.routing

import java.nio.file.Path
import java.nio.file.Paths

/**
 * An implementation of [GitHubAPI] which parses credentials from a file (the
 * `git-credential-store` file by default).
 */
class GitHubClient(
    gitHubCredentialParser: GitHubCredentialParser,
    credentialFilePath: Path
) : GitHubAPI by gitHubCredentialParser.parseCredentials(credentialFilePath).createRoutes() {

    companion object {
        val defaultCredentialFilePath by lazy {
            Paths.get(System.getProperty("user.home"), ".git-credentials")
        }

        private fun Pair<String, String>.createRoutes() = GitHubRoutes(first, second)
    }
}
