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

import arrow.core.Either
import com.commonwealthrobotics.bowlerkernel.authservice.AnonymousCredentialsProvider
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Timeout(value = 30, unit = TimeUnit.SECONDS)
class GitHubFSIntegTest {

    private val orgName = "CommonWealthRobotics"
    private val repoName = "bowler-kernel-test-repo"

    @Test
    fun `test gitUrlToDirectory with git repo`(@TempDir tempDir: File) {
        val expected = Paths.get(tempDir.absolutePath, orgName, repoName)

        val actual = GitHubFS.gitUrlToDirectory(
            tempDir.toPath(),
            testRepoUrlHTTPS
        )

        expected.toString().shouldBe(actual.absolutePath)
    }

    @Test
    fun `cannot clone over http`() {
        // Don't use a TempDir because jgit leaves something open so Windows builds fail
        val tmpCachePath = getRandomTempFile()

        val fs = GitHubFS(AnonymousCredentialsProvider, tmpCachePath)
        fs.cloneRepo(testRepoUrlHTTP).attempt().unsafeRunSync().shouldBeLeft()
    }

    @Test
    fun `test cloning test repo over https`() {
        // Don't use a TempDir because jgit leaves something open so Windows builds fail
        val tmpCachePath = getRandomTempFile()

        val fs = GitHubFS(AnonymousCredentialsProvider, tmpCachePath)
        val repoPath = fs.gitHubCacheDirectory.resolve(orgName).resolve(repoName)

        val files = cloneAndGetFileSet(fs, testRepoUrlHTTPS)
        assertTestRepoContents(files, repoPath)
    }

    @Test
    fun `test cloning with corrupted git folder`() {
        // Don't use a TempDir because jgit leaves something open so Windows builds fail
        val tmpCachePath = getRandomTempFile()
        val fs = GitHubFS(AnonymousCredentialsProvider, tmpCachePath)

        val repoPath = fs.gitHubCacheDirectory.resolve(orgName).resolve(repoName)

        // Make an empty .git directory so it appears corrupted
        repoPath.toFile().apply { mkdirs() }
        Paths.get(repoPath.toString(), ".git").toFile().apply { mkdirs() }

        val files = cloneAndGetFileSet(fs, testRepoUrlHTTPS)
        assertTestRepoContents(files, repoPath)
    }

    @Test
    fun `test cloning with some local changes`() {
        // Don't use a TempDir because jgit leaves something open so Windows builds fail
        val tmpCachePath = getRandomTempFile()

        val fs = GitHubFS(AnonymousCredentialsProvider, tmpCachePath)
        val repoPath = fs.gitHubCacheDirectory.resolve(orgName).resolve(repoName)

        // Clone the test repo and make sure it worked -- this is our baseline
        var files = cloneAndGetFileSet(fs, testRepoUrlHTTPS)
        assertTestRepoContents(files, repoPath)

        // Reset the test repo back one commit so that there is something to pull
        ProcessBuilder("git", "reset", "--hard", "HEAD^").directory(repoPath.toFile()).start().waitFor()

        // Make some changes that will need to be reset
        fs.getFilesInRepo(repoPath.toFile()).unsafeRunSync().forEach { it.deleteRecursively() }

        // Try to clone again. The repo should be reset and then pulled. Because we deleted all the files in the
        // previous commit, git may be able to fast-forward this pull; however, those files will still be deleted. A
        // correct GitFS implementation will reset the repo.
        files = cloneAndGetFileSet(fs, testRepoUrlHTTPS)
        assertTestRepoContents(files, repoPath)
    }

    @Test
    fun `test deleteCache`(@TempDir tempDir: File) {
        val fs = GitHubFS(AnonymousCredentialsProvider, tempDir.toPath())

        val repoPath = Paths.get(tempDir.absolutePath, orgName, repoName)

        val repo = repoPath.toFile().apply { mkdirs() }
        val fileInRepo = Paths.get(repoPath.toString(), "fileA.txt").toFile().apply {
            writeText("")
        }

        fileInRepo.shouldExist()

        fs.deleteCache().unsafeRunSync()

        fileInRepo.shouldNotExist()
        repo.shouldNotExist()
    }

    private fun cloneAndGetFileSet(fs: GitHubFS, repoURL: String): Either<Throwable, Set<String>> {
        return fs.cloneRepo(repoURL)
            .flatMap { fs.getFilesInRepo(it) }
            .map { files -> files.map { it.toString() }.toSet() }
            .attempt()
            .unsafeRunSync()
    }

    private fun assertTestRepoContents(files: Either<Throwable, Set<String>>, repoPath: Path) {
        files.shouldBeRight {
            it.shouldContainAll(
                Paths.get(repoPath.toString(), "fileA.txt").toString(),
                Paths.get(repoPath.toString(), "dirA").toString(),
                Paths.get(repoPath.toString(), "dirA", "fileB.txt").toString()
            )
        }
    }

    private fun getRandomTempFile() = Paths.get(
        System.getProperty("java.io.tmpdir"),
        Random.nextBytes(15).joinToString(separator = "").replace("-", "")
    )

    companion object {

        private const val testRepoUrlHTTP = "http://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git"
        private const val testRepoUrlHTTPS = "https://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git"
    }
}
