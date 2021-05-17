/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.commonwealthrobotics.bowlerkernel.authservice

import com.commonwealthrobotics.bowlerkernel.policyservice.PolicyService
import com.commonwealthrobotics.proto.policy.PolicyDocument

/**
 * Implements the [CredentialsProvider] interface using a [PolicyService].
 */
class PolicyBasedCredentialsProvider(
    private val policyService: PolicyService
) : CredentialsProvider {

    override suspend fun getCredentialsFor(remote: String): Credentials {
        val credentialsComponents = policyService.credentialsForScope(componentsForRemote(remote))
        return when {
            credentialsComponents.isEmpty() -> Credentials.Denied
            credentialsComponents[0] == "anonymous" -> Credentials.Anonymous
            credentialsComponents[0] == "api_key" -> Credentials.OAuth(credentialsComponents[1])
            else -> error("Unhandled credentials path: ${credentialsComponents[0]}")
        }
    }

    companion object {

        /**
         * @return The components (in a [PolicyDocument] sense) for the `remote`.
         */
        @Suppress("HttpUrlsUsage")
        internal fun componentsForRemote(remote: String): List<String> {
            val stripLeading = listOf("http://", "https://", "git@")
            val withoutProtocol = stripLeading.fold(remote) { str, elem -> str.split(elem).last() }
            return when {
                withoutProtocol.startsWith("github.com/") -> listOf(
                    "github",
                    "repo",
                    withoutProtocol.split("github.com/").last().split(".git").first()
                )
                withoutProtocol.startsWith("gist.github.com/") -> listOf(
                    "github",
                    "gist",
                    withoutProtocol.split("gist.github.com/").last().split(".git").first()
                )
                else -> error("Invalid remote: $remote")
            }
        }
    }
}
