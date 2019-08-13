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
package com.neuronrobotics.bowlerkernel.gitfs

import arrow.core.Either
import arrow.core.Option
import arrow.core.right
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.kohsuke.github.GitHub
import org.octogonapus.ktguava.collections.immutableSetOf
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class GitHubFSTest {

    private val testRepoUrl = "https://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git"

    @ParameterizedTest
    @MethodSource("isRepoUrlSource")
    fun `test isRepoUrl`(data: Pair<String, Boolean>) {
        assertThat(GitHubFS.isRepoUrl(data.first), equalTo(data.second))
    }

    @ParameterizedTest
    @MethodSource("isGistUrlSource")
    fun `test isGistUrl`(data: Pair<String, Boolean>) {
        assertThat(GitHubFS.isGistUrl(data.first), equalTo(data.second))
    }

    @ParameterizedTest
    @MethodSource("stripUrlCharactersFromGitUrlSource")
    fun `test stripUrlCharactersFromGitUrl`(data: Pair<String, String>) {
        assertThat(GitHubFS.stripUrlCharactersFromGitUrl(data.first), equalTo(data.second))
    }

    @Nested
    inner class UsingTestRepo {

        private val orgName = "CommonWealthRobotics"
        private val repoName = "bowler-kernel-test-repo"

        @Test
        fun `test gitUrlToDirectory with git repo`(@TempDir tempDir: File) {
            val expected = Paths.get(tempDir.absolutePath, orgName, repoName)

            val actual = GitHubFS.gitUrlToDirectory(
                tempDir.absolutePath,
                testRepoUrl
            )

            assertThat(expected.toString(), equalTo(actual.absolutePath))
        }

        @Test
        fun `test cloning test repo`() {
            val fs = makeAnonymousGitHubFS()

            // Don't use a TempDir because jgit leaves something open so Windows builds fail
            val repoPath = getCleanedRepoPath(fs)

            val files = fs.cloneRepoAndGetFiles(testRepoUrl)
                .map { files -> files.map { it.toString() }.toSet() }
                .attempt()
                .unsafeRunSync()

            assertEquals(
                immutableSetOf(
                    Paths.get(repoPath.toString(), "fileA.txt").toString(),
                    Paths.get(repoPath.toString(), "dirA").toString(),
                    Paths.get(repoPath.toString(), "dirA", "fileB.txt").toString()
                ).right(),
                files
            )
        }

        @Test
        fun `test cloning with corrupted git folder`() {
            val fs = makeAnonymousGitHubFS()

            // Don't use a TempDir because jgit leaves something open so Windows builds fail
            val repoPath = getCleanedRepoPath(fs)

            repoPath.toFile().apply { mkdirs() }
            Paths.get(repoPath.toString(), ".git").toFile().apply { mkdirs() }

            val files = fs.cloneRepoAndGetFiles(testRepoUrl)
                .map { files -> files.map { it.toString() }.toSet() }
                .attempt()
                .unsafeRunSync()

            assertEquals(
                immutableSetOf(
                    Paths.get(repoPath.toString(), "fileA.txt").toString(),
                    Paths.get(repoPath.toString(), "dirA").toString(),
                    Paths.get(repoPath.toString(), "dirA", "fileB.txt").toString()
                ).right(),
                files
            )
        }

        private fun makeAnonymousGitHubFS() =
            GitHubFS(GitHub.connectAnonymously(), "" to "")

        private fun getCleanedRepoPath(fs: GitHubFS) =
            Paths.get(fs.gitHubCacheDirectory, orgName, repoName).also {
                it.toFile().deleteRecursively()
            }

        @Test
        fun `test deleteCache`(@TempDir tempDir: File) {
            val fs = GitHubFS(
                GitHub.connectAnonymously(),
                "" to "",
                tempDir.absolutePath
            )

            val repoPath = Paths.get(tempDir.absolutePath, orgName, repoName)

            val repo = repoPath.toFile().apply { mkdirs() }
            val fileInRepo = Paths.get(repoPath.toString(), "fileA.txt").toFile().apply {
                writeText("")
            }

            assertTrue(fileInRepo.exists())

            fs.deleteCache()

            assertAll(
                { assertFalse(fileInRepo.exists()) },
                { assertFalse(repo.exists()) }
            )
        }
    }

    @Test
    fun `test cloning from invalid git url`(@TempDir tempDir: File) {
        val fs = GitHubFS(
            GitHub.connectAnonymously(),
            "" to "",
            tempDir.absolutePath
        )

        val actual = fs.cloneRepo("invalidGitRepo").attempt().unsafeRunSync()

        assertTrue(actual is Either.Left)
        actual as Either.Left

        assertTrue(actual.a is IllegalArgumentException)
    }

    @ParameterizedTest
    @MethodSource("parseRepoSource")
    fun `test parseRepo`(input: String, expected: Option<GitHubRepo>) {
        assertEquals(expected, GitHubFS.parseRepo(input))
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun isRepoUrlSource() = listOf(
            "" to false,
            "https://github.com/CommonWealthRobotics/BowlerBuilder.git" to true,
            "http://github.com/CommonWealthRobotics/BowlerBuilder.git" to true,
            "https://github.com/CommonWealthRobotics/BowlerBuilder.git/" to true,
            "http://github.com/CommonWealthRobotics/BowlerBuilder.git/" to true,
            "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git" to false,
            "http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git" to false,
            "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/" to false,
            "http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/" to false
        )

        @Suppress("unused")
        @JvmStatic
        fun isGistUrlSource() = listOf(
            "" to false,
            "https://github.com/CommonWealthRobotics/BowlerBuilder.git" to false,
            "http://github.com/CommonWealthRobotics/BowlerBuilder.git" to false,
            "https://github.com/CommonWealthRobotics/BowlerBuilder.git/" to false,
            "http://github.com/CommonWealthRobotics/BowlerBuilder.git/" to false,
            "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git" to true,
            "http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git" to true,
            "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/" to true,
            "http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/" to true
        )

        @Suppress("unused")
        @JvmStatic
        fun stripUrlCharactersFromGitUrlSource(): List<Pair<String, String>> {
            val repoOwnerAndName = "CommonWealthRobotics/BowlerBuilder"
            val gistId = "5681d11165708c3aec1ed5cf8cf38238"
            return listOf(
                "https://github.com/CommonWealthRobotics/BowlerBuilder.git" to
                    repoOwnerAndName,
                "http://github.com/CommonWealthRobotics/BowlerBuilder.git" to
                    repoOwnerAndName,
                "https://github.com/CommonWealthRobotics/BowlerBuilder.git/" to
                    repoOwnerAndName,
                "http://github.com/CommonWealthRobotics/BowlerBuilder.git/" to
                    repoOwnerAndName,
                "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git" to
                    gistId,
                "http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git" to
                    gistId,
                "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/" to
                    gistId,
                "http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/" to
                    gistId
            )
        }

        @Suppress("unused")
        @JvmStatic
        fun parseRepoSource() = listOf(
            Arguments.of(
                "https://github.com/CommonWealthRobotics/BowlerBuilder.git",
                Option.just(GitHubRepo.Repository("CommonWealthRobotics", "BowlerBuilder"))
            ),
            Arguments.of(
                "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git",
                Option.just(GitHubRepo.Gist("5681d11165708c3aec1ed5cf8cf38238"))
            ),
            Arguments.of(
                "invalidUrl",
                Option.empty<GitHubRepo>()
            )
        )
    }
}
