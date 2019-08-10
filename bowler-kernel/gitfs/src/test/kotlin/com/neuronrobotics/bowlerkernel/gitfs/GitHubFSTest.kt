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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
        assertEquals(data.second, GitHubFS.isRepoUrl(data.first))
    }

    @ParameterizedTest
    @MethodSource("isGistUrlSource")
    fun `test isGistUrl`(data: Pair<String, Boolean>) {
        assertEquals(data.second, GitHubFS.isGistUrl(data.first))
    }

    @ParameterizedTest
    @MethodSource("stripUrlCharactersFromGitUrlSource")
    fun `test stripUrlCharactersFromGitUrl`(data: Pair<String, String>) {
        assertEquals(data.second, GitHubFS.stripUrlCharactersFromGitUrl(data.first))
    }

    @Test
    fun `test gitUrlToDirectory with git repo`(@TempDir tempDir: File) {
        val expected = "${tempDir.absolutePath}/CommonWealthRobotics/bowler-kernel-test-repo"

        val actual = GitHubFS.gitUrlToDirectory(
            tempDir.absolutePath,
            testRepoUrl
        )

        assertEquals(expected, actual.absolutePath) {
            """
            Expected: $expected
            Actual: $actual
            """.trimIndent()
        }
    }

    @Test
    fun `test cloning test repo`(@TempDir tempDir: File) {
        val fs = GitHubFS(
            GitHub.connectAnonymously(),
            "" to "",
            tempDir.absolutePath
        )

        val files = fs.cloneRepoAndGetFiles(testRepoUrl)
            .map { files -> files.map { it.toString() }.toSet() }
            .attempt()
            .unsafeRunSync()

        val repoPath = Paths.get(
            tempDir.absolutePath,
            "CommonWealthRobotics",
            "bowler-kernel-test-repo"
        )

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
    fun `test cloning with corrupted git folder`(@TempDir tempDir: File) {
        val fs = GitHubFS(
            GitHub.connectAnonymously(),
            "" to "",
            tempDir.absolutePath
        )

        val repoPath = Paths.get(
            tempDir.absolutePath,
            "CommonWealthRobotics",
            "bowler-kernel-test-repo"
        )

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

    @Test
    fun `test deleteCache`(@TempDir tempDir: File) {
        val fs = GitHubFS(
            GitHub.connectAnonymously(),
            "" to "",
            tempDir.absolutePath
        )

        val repoPath = Paths.get(
            tempDir.absolutePath,
            "CommonWealthRobotics",
            "bowler-kernel-test-repo"
        )

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
        fun stripUrlCharactersFromGitUrlSource() = listOf(
            "https://github.com/CommonWealthRobotics/BowlerBuilder.git" to
                "CommonWealthRobotics/BowlerBuilder",
            "http://github.com/CommonWealthRobotics/BowlerBuilder.git" to
                "CommonWealthRobotics/BowlerBuilder",
            "https://github.com/CommonWealthRobotics/BowlerBuilder.git/" to
                "CommonWealthRobotics/BowlerBuilder",
            "http://github.com/CommonWealthRobotics/BowlerBuilder.git/" to
                "CommonWealthRobotics/BowlerBuilder",
            "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git" to
                "5681d11165708c3aec1ed5cf8cf38238",
            "http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git" to
                "5681d11165708c3aec1ed5cf8cf38238",
            "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/" to
                "5681d11165708c3aec1ed5cf8cf38238",
            "http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/" to
                "5681d11165708c3aec1ed5cf8cf38238"
        )

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
