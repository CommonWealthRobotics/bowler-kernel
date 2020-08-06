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

import arrow.core.Eval
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.option.applicative.just
import arrow.fx.IO
import arrow.fx.handleErrorWith
import com.commonwealthrobotics.bowlerkernel.util.BOWLERKERNEL_DIRECTORY
import com.commonwealthrobotics.bowlerkernel.util.BOWLER_DIRECTORY
import com.commonwealthrobotics.bowlerkernel.util.GITHUB_CACHE_DIRECTORY
import com.commonwealthrobotics.bowlerkernel.util.GIT_CACHE_DIRECTORY
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.kohsuke.github.GHGist
import org.kohsuke.github.GHGistFile
import org.kohsuke.github.GHObject
import org.kohsuke.github.GitHub
import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Paths

/**
 * A [GitFS] which interfaces with GitHub.
 *
 * @param gitHub The [GitHub] to connect to GitHub with.
 * @param credentials The credentials to authenticate to GitHub with.
 */
@SuppressWarnings("LargeClass")
class GitHubFS(
    private val gitHub: GitHub,
    private val credentials: Pair<String, String>,
    internal val gitHubCacheDirectory: String = Paths.get(
        System.getProperty("user.home"),
        BOWLER_DIRECTORY,
        BOWLERKERNEL_DIRECTORY,
        GIT_CACHE_DIRECTORY,
        GITHUB_CACHE_DIRECTORY
    ).toString()
) : GitFS {

    private val myGists = Eval.later { gitHub.myself.listGists().toList() }
    private val myRepositories = Eval.later { gitHub.myself.allRepositories.values }

    override fun cloneRepo(
        gitUrl: String,
        branch: String
    ): IO<File> {
        return freshClone(gitUrl, branch).handleErrorWith {
            val directory = gitUrlToDirectory(gitHubCacheDirectory, gitUrl)

            IO {
                // The repo was already on disk, so directory exists, so pull
                LOGGER.info { "Pulling repository in directory $directory" }
                Git.open(directory).use { it.pull().call() }
            }.handleErrorWith {
                if (it is RepositoryNotFoundException) {
                    IO.defer {
                        // The directory was not a valid repo
                        LOGGER.catching(it)
                        LOGGER.warn {
                            "Failed to pull from repo in $directory. Deleting directory."
                        }

                        if (!directory.deleteRecursively()) {
                            LOGGER.error { "Failed to delete $directory" }
                            throw IllegalStateException("Failed to delete $directory")
                        }

                        // After deleting the directory, try to clone again
                        freshClone(gitUrl, branch)
                    }
                } else {
                    IO.raiseError(it)
                }
            }.map { directory }
        }
    }

    /**
     * Tries to clone the repo assuming it is not on disk. Fails if any part of the repo is on
     * disk.
     *
     * @param gitUrl The `.git` URL to clone from, i.e.
     * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
     * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
     * @param branch The branch to checkout.
     * @return The directory of the cloned repository.
     */
    private fun freshClone(
        gitUrl: String,
        branch: String
    ): IO<File> {
        LOGGER.info {
            """
            |Cloning repository:
            |gitUrl: $gitUrl
            |branch: $branch
            """.trimMargin()
        }

        return if (isValidHttpGitURL(gitUrl)) {
            val directory = gitUrlToDirectory(gitHubCacheDirectory, gitUrl)
            if (directory.mkdirs()) {
                IO { cloneRepository(gitUrl, branch, directory) }.map { directory }
            } else {
                IO.raiseError(IllegalStateException("Directory $directory already exists."))
            }
        } else {
            IO.raiseError(
                IllegalArgumentException(
                    """
                    |Invalid git URL:
                    |$gitUrl
                    """.trimMargin()
                )
            )
        }
    }

    private fun cloneRepository(
        gitUrl: String,
        branch: String,
        directory: File
    ) {
        Git.cloneRepository()
            .setURI(gitUrl)
            .setBranch(branch)
            .setDirectory(directory)
            .setCredentialsProvider(
                UsernamePasswordCredentialsProvider(
                    credentials.first,
                    credentials.second
                )
            )
            .call()
            .use {
                Git.wrap(it.repository).use {
                    it.submoduleInit().call()
                    it.submoduleUpdate().call()
                }
            }
    }

    override fun cloneRepoAndGetFiles(
        gitUrl: String,
        branch: String
    ): IO<Set<File>> = mapToRepoFiles(cloneRepo(gitUrl, branch))

    override fun forkRepo(gitUrl: String): IO<String> =
        when (val repo = parseRepo(gitUrl)) {
            is None -> IO.raiseError(IllegalArgumentException("Invalid git url: $gitUrl"))
            is Some -> forkRepo(repo.t).map {
                it.url.toExternalForm()
            }
        }

    override fun forkAndCloneRepo(
        gitUrl: String,
        branch: String
    ): IO<File> = when (val repo = parseRepo(gitUrl)) {
        is None -> IO.raiseError(IllegalArgumentException("Invalid git url: $gitUrl"))
        is Some -> forkRepo(repo.t).flatMap {
            cloneRepo(it.gitUrl, branch)
        }
    }

    override fun forkAndCloneRepoAndGetFiles(
        gitUrl: String,
        branch: String
    ): IO<Set<File>> = mapToRepoFiles(forkAndCloneRepo(gitUrl, branch))

    override fun isOwner(gitUrl: String): IO<Boolean> = IO {
        myGists.value.firstOrNull {
            it.gitPullUrl == gitUrl
        } != null
    }.handleErrorWith {
        IO {
            myRepositories.value.first { repo ->
                repo.gitTransportUrl == gitUrl
            }.hasPushAccess()
        }
    }

    override fun deleteCache() {
        try {
            FileUtils.deleteDirectory(Paths.get(gitHubCacheDirectory).toFile())
        } catch (e: IOException) {
            LOGGER.error(e) {
                "Unable to delete the GitHub cache."
            }
        }
    }

    /**
     * Forks a repository.
     *
     * @param githubRepo The repo to fork.
     * @return The fork of the repository.
     */
    private fun forkRepo(
        githubRepo: GitHubRepo
    ): IO<GHObject> {
        LOGGER.info {
            """
            |Forking repository:
            |$githubRepo
            """.trimMargin()
        }

        return IO {
            when (githubRepo) {
                is GitHubRepo.Repository -> {
                    val repoFullName = "${githubRepo.owner}/${githubRepo.name}"
                    gitHub.getRepository(repoFullName).fork()
                }

                is GitHubRepo.Gist -> gitHub.getGist(githubRepo.gistId).fork()
            }
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }

        /**
         * Maps a file in a gist to its file on disk. Fails if the file is not on disk.
         *
         * @param gitHubCacheDirectory The directory path GitHub files are cached in.
         * @param gist The gist.
         * @param gistFile The file in the gist.
         * @return The file on disk.
         */
        fun mapGistFileToFileOnDisk(
            gitHubCacheDirectory: String,
            gist: GHGist,
            gistFile: GHGistFile
        ): IO<File> = IO {
            gitUrlToDirectory(gitHubCacheDirectory, gist.gitPullUrl).walkTopDown()
                .first { it.name == gistFile.fileName }
        }

        /**
         * Returns whether the [url] is a valid GitHub repository Git URL.
         *
         * @param url The URL to validate.
         * @return Whether the [url] is a valid GitHub repository Git URL.
         */
        fun isRepoUrl(url: String) =
            url.run {
                (startsWith("http://github.com/") || startsWith("https://github.com/")) &&
                    (endsWith(".git/") || endsWith(".git")) &&
                    isValidHttpGitURL(url)
            }

        /**
         * Returns whether the [url] is a valid GitHub Gist Git URL.
         *
         * @param url The URL to validate.
         * @return Whether the [url] is a valid GitHub Gist Git URL.
         */
        fun isGistUrl(url: String) =
            url.run {
                (
                    startsWith("http://gist.github.com/") ||
                        startsWith("https://gist.github.com/")
                    ) &&
                    (endsWith(".git/") || endsWith(".git")) &&
                    isValidHttpGitURL(url)
            }

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
         * Returns whether the [url] is a valid HTTP Git URL.
         *
         * @param url The URL to validate.
         * @return Whether the [url] is a valid HTTP Git URL.
         */
        private fun isValidHttpGitURL(url: String) =
            url.matches("(http(s)?)(:(//)?)([\\w.@:/\\-~]+)(\\.git)(/)?".toRegex())

        /**
         * Maps a Git URL to a [GitHubRepo].
         *
         * @param gitUrl The Git URL.
         * @return The [GitHubRepo] representation.
         */
        internal fun parseRepo(gitUrl: String): Option<GitHubRepo> {
            return when {
                isRepoUrl(gitUrl) -> {
                    val repoFullName = stripUrlCharactersFromGitUrl(gitUrl)
                    val (owner, repoName) = repoFullName.split("/")
                    GitHubRepo.Repository(owner, repoName).just()
                }

                isGistUrl(gitUrl) -> GitHubRepo.Gist(stripUrlCharactersFromGitUrl(gitUrl)).just()

                else -> Option.empty()
            }
        }

        /**
         * Maps a repository to its files.
         *
         * @param repoIO The repository.
         * @return The files in the repository, excluding `.git/` files and the receiver file.
         */
        internal fun mapToRepoFiles(repoIO: IO<File>) = repoIO.map { repoFile ->
            repoFile.walkTopDown()
                .filter { file -> file.path != repoFile.path }
                .filter { !it.path.contains(".git") }
                .toSet()
        }

        /**
         * Maps a [gitUrl] to its directory on disk. The directory does not necessarily exist.
         *
         * @param gitHubCacheDirectory The directory path GitHub files are cached in.
         * @param gitUrl The `.git` URL, i.e.
         * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
         * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
         */
        @SuppressWarnings("SpreadOperator")
        internal fun gitUrlToDirectory(gitHubCacheDirectory: String, gitUrl: String): File {
            require(isRepoUrl(gitUrl) || isGistUrl(gitUrl)) {
                "The supplied Git URL ($gitUrl) was not a valid repository or Gist URL."
            }

            val subDirs = stripUrlCharactersFromGitUrl(gitUrl)
                .split(FileSystems.getDefault().separator)

            return Paths.get(
                gitHubCacheDirectory,
                * subDirs.toTypedArray()
            ).toFile()
        }

        private val GHObject.gitUrl get() = "${htmlUrl.toExternalForm()}.git"
    }
}
