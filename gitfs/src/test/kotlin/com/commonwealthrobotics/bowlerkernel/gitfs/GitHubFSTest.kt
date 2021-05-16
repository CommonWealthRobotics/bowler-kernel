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
import com.commonwealthrobotics.bowlerkernel.authservice.DeniedCredentialsProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.concurrent.TimeUnit

@Suppress("HttpUrlsUsage")
@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class GitHubFSTest {

    @ParameterizedTest
    @MethodSource("stripUrlCharactersFromGitUrlSource")
    fun `test stripUrlCharactersFromGitUrl`(url: String, expected: String) {
        GitHubFS.stripUrlCharactersFromGitUrl(url) shouldBe expected
    }

    @Test
    fun `not allowed to clone from an invalid URL`(@TempDir tempDir: File) {
        val fs = GitHubFS(AnonymousCredentialsProvider, tempDir.toPath())
        runBlocking {
            shouldThrow<IllegalArgumentException> {
                fs.cloneRepo("invalidGitRepo", "HEAD")
            }
        }
    }

    @Test
    fun `not allowed to clone using bare HTTP`(@TempDir tempDir: File) {
        val fs = GitHubFS(AnonymousCredentialsProvider, tempDir.toPath())
        runBlocking {
            shouldThrow<UnsupportedOperationException> {
                fs.cloneRepo("http://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git", "HEAD")
            }
        }
    }

    @Test
    fun `not allowed to clone using denied credentials`(@TempDir tempDir: File) {
        val fs = GitHubFS(DeniedCredentialsProvider, tempDir.toPath())
        runBlocking {
            shouldThrow<UnsupportedOperationException> {
                fs.cloneRepo("https://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git", "HEAD")
            }
        }
    }

    @Test
    fun `not allowed to clone using denied credentials with a populated cache`(@TempDir tempDir: File) {
        val fsAnon = GitHubFS(AnonymousCredentialsProvider, tempDir.toPath())
        runBlocking {
            // Clone using anonymous credentials to populate the local cache with this repo
            fsAnon.cloneRepo("https://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git", "HEAD")

            // Even though this repo is in the cache now, cloning must not be allowed if the acting policy document
            // denies it
            val fsDenied = GitHubFS(DeniedCredentialsProvider, tempDir.toPath())
            shouldThrow<UnsupportedOperationException> {
                fsDenied.cloneRepo("https://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git", "HEAD")
            }
        }
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun stripUrlCharactersFromGitUrlSource(): List<Arguments> {
            val repoOwnerAndName = "CommonWealthRobotics/bowler-kernel-test-repo"
            val gistId = "5681d11165708c3aec1ed5cf8cf38238"
            return listOf(
                Arguments.of("https://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git", repoOwnerAndName),
                Arguments.of("http://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git", repoOwnerAndName),
                Arguments.of("https://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git/", repoOwnerAndName),
                Arguments.of("http://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git/", repoOwnerAndName),
                Arguments.of("https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git", gistId),
                Arguments.of("http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git", gistId),
                Arguments.of("https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/", gistId),
                Arguments.of("http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/", gistId),
            )
        }
    }
}
