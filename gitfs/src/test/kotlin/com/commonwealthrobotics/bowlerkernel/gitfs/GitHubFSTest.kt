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
package com.commonwealthrobotics.bowlerkernel.gitfs

import com.commonwealthrobotics.bowlerkernel.authservice.AnonymousCredentialsProvider
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class GitHubFSTest {

    @ParameterizedTest
    @MethodSource("stripUrlCharactersFromGitUrlSource")
    fun `test stripUrlCharactersFromGitUrl`(url: String, expected: String) {
        GitHubFS.stripUrlCharactersFromGitUrl(url) shouldBe expected
    }

    @Test
    fun `test cloning from invalid git url`(@TempDir tempDir: File) {
        val fs = GitHubFS(AnonymousCredentialsProvider, tempDir.toPath())
        val actual = fs.cloneRepo("invalidGitRepo").attempt().unsafeRunSync()
        actual.shouldBeLeft {
            it.shouldBeInstanceOf<IllegalArgumentException>()
        }
    }

    @Test
    fun `test cloning over http`(@TempDir tempDir: File) {
        val fs = GitHubFS(AnonymousCredentialsProvider, tempDir.toPath())
        val actual = fs.cloneRepo("http://github.com/CommonWealthRobotics/BowlerBuilder.git").attempt().unsafeRunSync()
        actual.shouldBeLeft {
            it.shouldBeInstanceOf<UnsupportedOperationException>()
        }
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun stripUrlCharactersFromGitUrlSource(): List<Arguments> {
            val repoOwnerAndName = "CommonWealthRobotics/BowlerBuilder"
            val gistId = "5681d11165708c3aec1ed5cf8cf38238"
            return listOf(
                Arguments.of("https://github.com/CommonWealthRobotics/BowlerBuilder.git", repoOwnerAndName),
                Arguments.of("http://github.com/CommonWealthRobotics/BowlerBuilder.git", repoOwnerAndName),
                Arguments.of("https://github.com/CommonWealthRobotics/BowlerBuilder.git/", repoOwnerAndName),
                Arguments.of("http://github.com/CommonWealthRobotics/BowlerBuilder.git/", repoOwnerAndName),
                Arguments.of("https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git", gistId),
                Arguments.of("http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git", gistId),
                Arguments.of("https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/", gistId),
                Arguments.of("http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/", gistId),
            )
        }
    }
}
