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

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PolicyBasedCredentialsProviderTest {

    @ParameterizedTest
    @MethodSource("remoteSource")
    fun `components for remote`(remote: String, expectedComponents: List<String>) {
        PolicyBasedCredentialsProvider.componentsForRemote(remote).shouldBe(expectedComponents)
    }

    @Test
    fun `deny remote`() {
        val provider = PolicyBasedCredentialsProvider(
            mockk {
                every { credentialsForScope(any()) } returns listOf()
            }
        )
        runBlocking {
            provider.getCredentialsFor("https://github.com/A/B.git").shouldBe(Credentials.Denied)
        }
    }

    @Test
    fun `anonymous remote`() {
        val provider = PolicyBasedCredentialsProvider(
            mockk {
                every { credentialsForScope(any()) } returns listOf("anonymous")
            }
        )
        runBlocking {
            provider.getCredentialsFor("https://github.com/A/B.git").shouldBe(Credentials.Anonymous)
        }
    }

    @Test
    fun `api key remote`() {
        val provider = PolicyBasedCredentialsProvider(
            mockk {
                every { credentialsForScope(any()) } returns listOf("api_key", "ds8h0fg8h0")
            }
        )
        runBlocking {
            provider.getCredentialsFor("https://github.com/A/B.git").shouldBe(Credentials.OAuth("ds8h0fg8h0"))
        }
    }

    companion object {

        @Suppress("unused", "HttpUrlsUsage")
        @JvmStatic
        fun remoteSource() = listOf(
            Arguments.of(
                "https://github.com/CommonWealthRobotics/BowlerBuilder.git",
                listOf("github", "repo", "CommonWealthRobotics/BowlerBuilder")
            ),
            Arguments.of(
                "https://github.com/CommonWealthRobotics/BowlerBuilder",
                listOf("github", "repo", "CommonWealthRobotics/BowlerBuilder")
            ),
            Arguments.of(
                "http://github.com/CommonWealthRobotics/BowlerBuilder.git",
                listOf("github", "repo", "CommonWealthRobotics/BowlerBuilder")
            ),
            Arguments.of(
                "http://github.com/CommonWealthRobotics/BowlerBuilder",
                listOf("github", "repo", "CommonWealthRobotics/BowlerBuilder")
            ),
            Arguments.of(
                "git@github.com/CommonWealthRobotics/BowlerBuilder.git",
                listOf("github", "repo", "CommonWealthRobotics/BowlerBuilder")
            ),
            Arguments.of(
                "git@github.com/CommonWealthRobotics/BowlerBuilder",
                listOf("github", "repo", "CommonWealthRobotics/BowlerBuilder")
            ),
            Arguments.of(
                "https://gist.github.com/Octogonapus/39627229040b15ee43c70ae613d0698d.git",
                listOf("github", "gist", "Octogonapus/39627229040b15ee43c70ae613d0698d")
            ),
            Arguments.of(
                "https://gist.github.com/Octogonapus/39627229040b15ee43c70ae613d0698d",
                listOf("github", "gist", "Octogonapus/39627229040b15ee43c70ae613d0698d")
            ),
            Arguments.of(
                "http://gist.github.com/Octogonapus/39627229040b15ee43c70ae613d0698d.git",
                listOf("github", "gist", "Octogonapus/39627229040b15ee43c70ae613d0698d")
            ),
            Arguments.of(
                "http://gist.github.com/Octogonapus/39627229040b15ee43c70ae613d0698d",
                listOf("github", "gist", "Octogonapus/39627229040b15ee43c70ae613d0698d")
            ),
            Arguments.of(
                "git@gist.github.com/Octogonapus/39627229040b15ee43c70ae613d0698d.git",
                listOf("github", "gist", "Octogonapus/39627229040b15ee43c70ae613d0698d")
            ),
            Arguments.of(
                "git@gist.github.com/Octogonapus/39627229040b15ee43c70ae613d0698d",
                listOf("github", "gist", "Octogonapus/39627229040b15ee43c70ae613d0698d")
            ),
        )
    }
}
