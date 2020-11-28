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

import arrow.fx.IO
import arrow.fx.extensions.fx
import com.commonwealthrobotics.proto.gitfs.FileSpec
import com.commonwealthrobotics.proto.gitfs.ProjectSpec
import java.io.File

/**
 * @param gitFS The [GitFS] used to download dependencies.
 */
class DefaultDependencyResolver(
    private val gitFS: GitFS
) : DependencyResolver {

    override fun resolve(fileSpec: FileSpec): File {
        return IO.fx {
            // After cloneRepo, the cached copy of the repo is consistent with the remote, so there should be no need
            // to reset any possibly previously applied patch.
            val repoDir = gitFS.cloneRepo(fileSpec.project.repoRemote, fileSpec.project.revision).bind()

            val patch = fileSpec.project.patch.patch
            if (!patch.isEmpty) {
                // TODO: Apply the patch using jgit
                val proc = ProcessBuilder("git", "apply", "-").directory(repoDir).start()
                proc.outputStream.write(patch.toByteArray())
                proc.outputStream.close()
                val exitCode = proc.waitFor()
                check(exitCode == 0) {
                    "Failed to apply the patch (exit code $exitCode) when resolving $fileSpec"
                }
            }

            val filesInRepo = gitFS.getFilesInRepo(repoDir).bind()
            val filesString = filesInRepo.joinToString("\n") { "\t$it" }
            filesInRepo.firstOrNull { it.relativeTo(repoDir).path == fileSpec.path }
                ?: throw IllegalStateException(
                    """
                    |Cannot resolve $fileSpec
                    |Contents of $repoDir:
                    |$filesString
                    """.trimMargin()
                )
        }.unsafeRunSync()
    }

    override fun addDev(dev: ProjectSpec) {
        // TODO: Implement me
    }

    override fun addDevs(devs: List<ProjectSpec>) {
        // TODO: Implement me
    }
}
