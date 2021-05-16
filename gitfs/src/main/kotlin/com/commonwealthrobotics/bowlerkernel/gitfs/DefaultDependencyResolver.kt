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

import com.commonwealthrobotics.proto.gitfs.FileSpec
import com.commonwealthrobotics.proto.gitfs.ProjectSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @param gitFS The [GitFS] used to download dependencies.
 */
class DefaultDependencyResolver(
    private val gitFS: GitFS
) : DependencyResolver {

    override suspend fun resolve(fileSpec: FileSpec): File {
        // After cloneRepo, the cached copy of the repo is consistent with the remote, so there should be no need
        // to reset any possibly previously applied patch.
        val repoDir = gitFS.cloneRepo(fileSpec.project.repoRemote, fileSpec.project.revision)

        val patch = fileSpec.project.patch.patch
        if (!patch.isEmpty) {
            @Suppress("BlockingMethodInNonBlockingContext") // Not blocking because it's lifted via withContext
            withContext(Dispatchers.IO) {
                // TODO: Apply the patch using jgit
                val proc = ProcessBuilder("git", "apply", "-").directory(repoDir).start()
                proc.outputStream.write(patch.toByteArray())
                proc.outputStream.close()
                val exitCode = proc.waitFor()

                check(exitCode == 0) {
                    """
                    |Failed to apply the patch (exit code $exitCode) when resolving:
                    |$fileSpec
                    |======================
                    |       stdout:
                    |======================
                    |${proc.inputStream.readAllBytes().decodeToString()}
                    |======================
                    |       stderr:
                    |======================
                    |${proc.errorStream.readAllBytes().decodeToString()}
                    """.trimMargin()
                }
            }
        }

        val filesInRepo = gitFS.getFilesInRepo(repoDir)
        val filesString = filesInRepo.joinToString("\n") { "\t$it" }
        return filesInRepo.firstOrNull { it.relativeTo(repoDir).path == fileSpec.path }
            ?: throw IllegalStateException(
                """
                |Cannot resolve $fileSpec
                |Contents of $repoDir:
                |$filesString
                """.trimMargin()
            )
    }

    override suspend fun addDev(dev: ProjectSpec) {
        // TODO: Implement me
    }

    override suspend fun addDevs(devs: List<ProjectSpec>) {
        // TODO: Implement me
    }
}
