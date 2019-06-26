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

package com.neuronrobotics.bowlerkernel.gitfs

import arrow.effects.IO
import arrow.effects.handleErrorWith
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.settings.BOWLERKERNEL_DIRECTORY
import com.neuronrobotics.bowlerkernel.settings.BOWLER_DIRECTORY
import com.neuronrobotics.bowlerkernel.settings.GITHUB_CACHE_DIRECTORY
import com.neuronrobotics.bowlerkernel.settings.GIT_CACHE_DIRECTORY
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.kohsuke.github.GHGist
import org.kohsuke.github.GHGistFile
import org.kohsuke.github.GHObject
import org.kohsuke.github.GitHub
import org.octogonapus.ktguava.collections.toImmutableList
import java.io.File
import java.io.IOException
import java.nio.file.Paths

/**
 * A [GitFS] which interfaces with GitHub.
 *
 * @param gitHub The [GitHub] to connect to GitHub with.
 * @param credentials The credentials to authenticate to GitHub with.
 */
class GitHubFS(
    private val gitHub: GitHub,
    private val credentials: Pair<String, String>
) : GitFS {

    override fun cloneRepo(
        gitUrl: String,
        branch: String
    ): IO<File> {
        return freshClone(gitUrl, branch).handleErrorWith {
            val directory = gitUrlToDirectory(gitUrl)

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
            val directory = gitUrlToDirectory(gitUrl)
            val result = if (directory.mkdirs()) {
                IO {
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
                            it.submoduleInit().call()
                            it.submoduleUpdate().call()
                        }
                }.map { directory }
            } else {
                IO.raiseError(IllegalStateException("Directory $directory already exists."))
            }

            result
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

    override fun cloneRepoAndGetFiles(
        gitUrl: String,
        branch: String
    ): IO<ImmutableList<File>> = cloneRepo(gitUrl, branch).mapToRepoFiles()

    override fun forkRepo(gitUrl: String): IO<String> =
        forkRepo(parseRepo(gitUrl)).map {
            it.url.toExternalForm()
        }

    override fun forkAndCloneRepo(
        gitUrl: String,
        branch: String
    ): IO<File> = forkRepo(parseRepo(gitUrl)).flatMap {
        cloneRepo(it.gitUrl, branch)
    }

    override fun forkAndCloneRepoAndGetFiles(
        gitUrl: String,
        branch: String
    ): IO<ImmutableList<File>> = forkAndCloneRepo(gitUrl, branch).mapToRepoFiles()

    override fun isOwner(gitUrl: String): IO<Boolean> = IO {
        gitHub.myself.listGists().firstOrNull {
            it.gitPullUrl == gitUrl
        } != null
    }.handleErrorWith {
        IO {
            gitHub.myself.listRepositories().first { repo ->
                repo.gitTransportUrl == gitUrl
            }.hasPushAccess()
        }
    }

    override fun deleteCache() {
        try {
            FileUtils.deleteDirectory(
                Paths.get(
                    System.getProperty("user.home"),
                    BOWLER_DIRECTORY,
                    BOWLERKERNEL_DIRECTORY,
                    GIT_CACHE_DIRECTORY,
                    GITHUB_CACHE_DIRECTORY
                ).toFile()
            )
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
         * @param gist The gist.
         * @param gistFile The file in the gist.
         * @return The file on disk.
         */
        fun mapGistFileToFileOnDisk(gist: GHGist, gistFile: GHGistFile): IO<File> = IO {
            gitUrlToDirectory(gist.gitPullUrl).walkTopDown().first { it.name == gistFile.fileName }
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
                (startsWith("http://gist.github.com/") ||
                    startsWith("https://gist.github.com/")) &&
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
        private fun parseRepo(gitUrl: String): GitHubRepo {
            return when {
                isRepoUrl(gitUrl) -> {
                    val repoFullName = stripUrlCharactersFromGitUrl(gitUrl)
                    val (owner, repoName) = repoFullName.split("/")
                    GitHubRepo.Repository(owner, repoName)
                }

                isGistUrl(gitUrl) -> GitHubRepo.Gist(stripUrlCharactersFromGitUrl(gitUrl))

                else -> throw IllegalArgumentException(
                    """
                    |Invalid Git URL:
                    |$gitUrl
                    """.trimMargin()
                )
            }
        }

        /**
         * Maps a repository to its files.
         *
         * @receiver The repository.
         * @return The files in the repository, excluding `.git/` files and the receiver file.
         */
        private fun IO<File>.mapToRepoFiles() = map { repoFile ->
            repoFile.walkTopDown()
                .filter { file -> file.path != repoFile.path }
                .filter { !it.path.contains(".git") }
                .toList()
                .toImmutableList()
        }

        /**
         * Maps a [gitUrl] to its directory on disk. The directory does not necessarily exist.
         *
         * @param gitUrl The `.git` URL, i.e.
         * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
         * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
         */
        @SuppressWarnings("SpreadOperator")
        private fun gitUrlToDirectory(gitUrl: String): File {
            require(isRepoUrl(gitUrl) || isGistUrl(gitUrl)) {
                "The supplied Git URL ($gitUrl) was not a valid repository or Gist URL."
            }

            val subDirs = stripUrlCharactersFromGitUrl(gitUrl).split("/")

            return Paths.get(
                System.getProperty("user.home"),
                BOWLER_DIRECTORY,
                BOWLERKERNEL_DIRECTORY,
                GIT_CACHE_DIRECTORY,
                GITHUB_CACHE_DIRECTORY,
                * subDirs.toTypedArray()
            ).toFile()
        }

        private val GHObject.gitUrl get() = "${htmlUrl.toExternalForm()}.git"
    }
}
