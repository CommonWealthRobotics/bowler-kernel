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
@file:Suppress("ThrowableNotThrown")

package com.commonwealthrobotics.bowlerkernel.gitfs

import arrow.fx.IO
import arrow.fx.handleErrorWith
import com.commonwealthrobotics.bowlerkernel.authservice.Credentials
import com.commonwealthrobotics.bowlerkernel.authservice.CredentialsProvider
import com.commonwealthrobotics.bowlerkernel.util.getFullPathToGitHubCacheDirectory
import com.commonwealthrobotics.bowlerkernel.util.run
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * A [GitFS] which interfaces with GitHub.
 *
 * @param credentialsProvider The [CredentialsProvider] used to ask for `Git` credentials.
 * @param gitHubCacheDirectory The directory inside of which the GitHub cache (repos, gists, etc.) will be maintained.
 */
@SuppressWarnings("LargeClass")
class GitHubFS(
    private val credentialsProvider: CredentialsProvider,
    internal val gitHubCacheDirectory: Path = getFullPathToGitHubCacheDirectory()
) : GitFS {

    private val fsLock = ReentrantLock()

    override fun cloneRepo(
        gitUrl: String,
        revision: String
    ): IO<File> {
        return freshClone(gitUrl, revision).handleErrorWith {
            val directory = gitUrlToDirectory(gitHubCacheDirectory, gitUrl)

            IO {
                // The repo was already on disk, so directory exists, so pull
                logger.info { "Pulling repository in directory $directory" }
                @Suppress("BlockingMethodInNonBlockingContext")
                fsLock.withLock {
                    // TODO: Use JGit instead
                    run(directory, "git", "reset", "--hard", "HEAD")
                    run(directory, "git", "fetch")
                    run(directory, "git", "checkout", revision)
                }
            }.handleErrorWith {
                IO.defer {
                    // The directory was not a valid repo
                    logger.catching(it)
                    logger.warn {
                        "Failed to pull from repo in $directory. Deleting directory."
                    }

                    fsLock.withLock {
                        if (!directory.deleteRecursively()) {
                            logger.error { "Failed to delete $directory" }
                            throw IllegalStateException("Failed to delete $directory")
                        }

                        // After deleting the directory, try to clone again
                        freshClone(gitUrl, revision)
                    }
                }
            }.map { directory }
        }
    }

    override fun getFilesInRepo(repoDir: File): IO<Set<File>> = mapToRepoFiles(IO.just(repoDir))

    override fun deleteCache(): IO<Unit> = IO {
        @Suppress("BlockingMethodInNonBlockingContext")
        fsLock.withLock { FileUtils.deleteDirectory(gitHubCacheDirectory.toFile()) }
    }

    /**
     * Tries to clone the repo assuming it is not on disk. Fails if any part of the repo is on
     * disk.
     */
    private fun freshClone(
        gitUrl: String,
        revision: String
    ): IO<File> {
        logger.info {
            """
            |Cloning repository:
            |gitUrl: $gitUrl
            |revision: $revision
            """.trimMargin()
        }

        val directory = gitUrlToDirectory(gitHubCacheDirectory, gitUrl)
        return if (directory.mkdirs()) {
            IO { cloneRepository(gitUrl, revision, directory) }.map { directory }
        } else {
            IO.raiseError(IllegalStateException("Directory $directory already exists."))
        }
    }

    private suspend fun cloneRepository(
        gitUrl: String,
        revision: String,
        directory: File
    ) {
        if (gitUrl.startsWith("http://")) {
            throw UnsupportedOperationException(
                "Will not clone using bare HTTP (URL $gitUrl). Use HTTPS instead."
            )
        }

        val jGitCredentialsProvider = getJGitCredentialsProvider(gitUrl)
        fsLock.withLock {
            // Try to clone without credentials first. If we can't, then request credentials.
            val git = Git.cloneRepository()
                .setURI(gitUrl)
                .setDirectory(directory)
                .setCredentialsProvider(jGitCredentialsProvider)
                .call()

            git.use {
                Git.wrap(it.repository).use {
                    it.fetch().call()
                    it.checkout().setName(revision).call()
                    it.submoduleInit().call()
                    it.submoduleUpdate().call()
                }
            }
        }
    }

    private suspend fun getJGitCredentialsProvider(gitUrl: String): UsernamePasswordCredentialsProvider {
        require(gitUrl.endsWith(".git"))
        return when (val credentials = credentialsProvider.getCredentialsFor(gitUrl)) {
            is Credentials.Basic -> UsernamePasswordCredentialsProvider(credentials.username, credentials.password)
            // TODO: Validate this works
            is Credentials.OAuth -> UsernamePasswordCredentialsProvider(credentials.token, "x-oauth-basic")
            is Credentials.Anonymous -> UsernamePasswordCredentialsProvider("", "")
        }
    }

    /**
     * Maps a repository to its files.
     *
     * @param repoIO The repository.
     * @return The files in the repository, excluding `.git/` files and the receiver file.
     */
    private fun mapToRepoFiles(repoIO: IO<File>) = repoIO.map { repoFile ->
        repoFile.walkTopDown()
            .filter { file -> file.path != repoFile.path }
            .filter { !it.path.contains(".git") }
            .toSet()
    }

    companion object {

        private val logger = KotlinLogging.logger { }

        /**
         * Removes all the characters from [gitUrl] which are not part of the Gist id or
         * repository owner and name.
         *
         * @param gitUrl The URL to format.
         * @return The stripped URL.
         */
        fun stripUrlCharactersFromGitUrl(gitUrl: String) =
            gitUrl.removePrefix("http://github.com/")
                .removePrefix("https://github.com/")
                .removePrefix("http://gist.github.com/")
                .removePrefix("https://gist.github.com/")
                .removeSuffix(".git/")
                .removeSuffix(".git")

        /**
         * Maps a [gitUrl] to its directory on disk. The directory does not necessarily exist.
         *
         * @param gitHubCacheDirectory The directory path GitHub files are cached in.
         * @param gitUrl The `.git` URL, i.e.
         * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
         * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
         */
        @SuppressWarnings("SpreadOperator")
        internal fun gitUrlToDirectory(gitHubCacheDirectory: Path, gitUrl: String): File {
            val subDirs = stripUrlCharactersFromGitUrl(gitUrl)
                .split(FileSystems.getDefault().separator)

            return subDirs.fold(gitHubCacheDirectory) { acc, elem ->
                acc.resolve(elem)
            }.toFile()
        }
    }
}
