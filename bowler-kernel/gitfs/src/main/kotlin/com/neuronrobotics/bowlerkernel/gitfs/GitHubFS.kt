/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.gitfs

import arrow.core.Try
import arrow.core.recoverWith
import com.google.common.base.Throwables
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.internal.logging.LoggerUtilities
import com.neuronrobotics.bowlerkernel.settings.BOWLERKERNEL_DIRECTORY
import com.neuronrobotics.bowlerkernel.settings.BOWLER_DIRECTORY
import com.neuronrobotics.bowlerkernel.settings.GITHUB_CACHE_DIRECTORY
import com.neuronrobotics.bowlerkernel.settings.GIT_CACHE_DIRECTORY
import com.neuronrobotics.bowlerkernel.util.toImmutableList
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.kohsuke.github.GHGist
import org.kohsuke.github.GHGistFile
import org.kohsuke.github.GHObject
import org.kohsuke.github.GitHub
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
    ): Try<File> {
        return if (isValidHttpGitURL(gitUrl)) {
            val directory =
                gitUrlToDirectory(gitUrl)
            if (directory.mkdirs()) {
                // If true, the directories were created which means a new repository is
                // being cloned
                Try {
                    val git = Git.cloneRepository()
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

                    git.submoduleInit().call()
                    git.submoduleUpdate().call()
                    git.close()
                }.map {
                    directory
                }
            } else {
                // If false, the repository is already cloned, so pull instead
                Try {
                    Git.open(directory).pull().call()
                }.map {
                    directory
                }
            }
        } else {
            Try.raise(
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
    ): Try<ImmutableList<File>> = cloneRepo(gitUrl, branch).mapToRepoFiles()

    override fun forkRepo(gitUrl: String): Try<String> =
        forkRepo(parseRepo(gitUrl)).map {
            it.url.toExternalForm()
        }

    override fun forkAndCloneRepo(
        gitUrl: String,
        branch: String
    ): Try<File> {
        return Try {
            forkRepo(parseRepo(gitUrl)).flatMap {
                cloneRepo(it.url.toExternalForm(), branch)
            }
        }.flatMap { it }
    }

    private fun parseRepo(
        gitUrl: String
    ): GitHubRepo {
        return when {
            gitUrl.startsWith("http://github.com/") ||
                gitUrl.startsWith("https://github.com/") -> {
                val repoFullName = gitUrl
                    .removePrefix("http://github.com/")
                    .removePrefix("https://github.com/")
                    .removeSuffix(".git/")
                    .removeSuffix(".git")

                val (owner, repoName) = repoFullName.split("/")
                GitHubRepo.Repository(owner, repoName)
            }

            gitUrl.startsWith("http://gist.github.com/") ||
                gitUrl.startsWith("https://gist.github.com/") -> {
                val gistId = gitUrl
                    .removePrefix("http://gist.github.com/")
                    .removePrefix("https://gist.github.com/")
                    .removeSuffix(".git/")
                    .removeSuffix(".git")

                GitHubRepo.Gist(gistId)
            }

            else -> throw IllegalArgumentException(
                """
                |Invalid Git URL:
                |$gitUrl
                """.trimMargin()
            )
        }
    }

    override fun forkAndCloneRepoAndGetFiles(
        gitUrl: String,
        branch: String
    ): Try<ImmutableList<File>> = forkAndCloneRepo(gitUrl, branch).mapToRepoFiles()

    override fun isOwner(gitUrl: String): Try<Boolean> {
        return Try {
            gitHub.myself.listGists().firstOrNull {
                it.gitPullUrl == gitUrl
            } != null
        }.recoverWith {
            Try {
                gitHub.myself.listRepositories().first { repo ->
                    repo.gitTransportUrl == gitUrl
                }.hasPushAccess()
            }
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
            LOGGER.severe {
                """
                |Unable to delete the GitHub cache.
                |${Throwables.getStackTraceAsString(e)}
                """.trimMargin()
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
    ): Try<GHObject> {
        return Try {
            when (githubRepo) {
                is GitHubRepo.Repository -> {
                    val repoFullName = "${githubRepo.owner}/${githubRepo.name}"
                    gitHub.getRepository(repoFullName).fork()
                }

                is GitHubRepo.Gist -> gitHub.getGist(githubRepo.gistId).fork()
            }
        }
    }

    /**
     * Maps a repository to its files.
     *
     * @receiver The repository.
     * @return The files in the repository, excluding `.git/` files and the receiver file.
     */
    private fun Try<File>.mapToRepoFiles() = map { repoFile ->
        repoFile.walkTopDown()
            .filter { file -> file.path != repoFile.path }
            .filter { !it.path.contains(".git") }
            .toList()
            .toImmutableList()
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(GitHubFS::class.java.simpleName)

        /**
         * Maps a file in a gist to its file on disk. Fails if the file is not on disk.
         *
         * @param gist The gist.
         * @param gistFile The file in the gist.
         * @return The file on disk.
         */
        fun mapGistFileToFileOnDisk(gist: GHGist, gistFile: GHGistFile): Try<File> {
            val directory =
                gitUrlToDirectory(gist.gitPullUrl)

            return Try {
                directory.walkTopDown().first { it.name == gistFile.fileName }
            }
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
            val subDirs = gitUrl
                .removePrefix("http://github.com/")
                .removePrefix("https://github.com/")
                .removePrefix("http://gist.github.com/")
                .removePrefix("https://gist.github.com/")
                .removeSuffix(".git/")
                .removeSuffix(".git")
                .split("/")

            return Paths.get(
                System.getProperty("user.home"),
                BOWLER_DIRECTORY,
                BOWLERKERNEL_DIRECTORY,
                GIT_CACHE_DIRECTORY,
                GITHUB_CACHE_DIRECTORY,
                * subDirs.toTypedArray()
            ).toFile()
        }

        /**
         * Returns whether the [url] is a valid HTTP Git url.
         *
         * @param url The url to validate
         * @return Whether the [url] is a valid HTTP Git url.
         */
        private fun isValidHttpGitURL(url: String) =
            url.matches("(http(s)?)(:(//)?)([\\w.@:/\\-~]+)(\\.git)(/)?".toRegex())
    }
}
