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
import com.commonwealthrobotics.proto.gitfs.FileSpec
import com.google.protobuf.ByteString
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeZero
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class DefaultDependencyResolverTest {

    @Test
    fun `resolve a script with no patch and no devs`(@TempDir tempDir: File) {
        val fileToResolve = createTempFile(suffix = ".groovy", directory = tempDir)
        val file = FileSpec.newBuilder().apply {
            projectBuilder.repoRemote = "git@github.com:user/repo1.git"
            projectBuilder.revision = "master"
            projectBuilder.patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
            path = fileToResolve.name
        }.build()

        val gitFS = mockk<GitFS> {
            every { cloneRepo(file.project.repoRemote, file.project.revision) } returns IO.just(tempDir)
            every { getFilesInRepo(tempDir) } returns IO.just(
                setOf(
                    createTempFile(".groovy", directory = tempDir),
                    fileToResolve,
                    createTempFile(".groovy", directory = tempDir)
                )
            )
        }

        val resolver = DefaultDependencyResolver(gitFS)
        val resolvedFile = resolver.resolve(file)
        resolvedFile.shouldBe(fileToResolve)

        verifyOrder {
            gitFS.cloneRepo(file.project.repoRemote, file.project.revision)
            gitFS.getFilesInRepo(tempDir)
        }
    }

    @Test
    fun `resolve a script with a patch and no devs`(@TempDir tempDir: File) {
        // Make a file and write 1 into it
        val fileToResolve = createTempFile(suffix = ".groovy", directory = tempDir)
        fileToResolve.writeText("1")

        // Init a git repo, add all files, and commit
        runAndPrintOutput(tempDir, "git", "init", ".")
        runAndPrintOutput(tempDir, "git", "add", ".")
        runAndPrintOutput(tempDir, "git", "commit", "-m", "a")

        // Write 2 into that file and generate that diff
        fileToResolve.writeText("2")

        val diff = run(tempDir, "git", "diff", "HEAD").inputStream.readAllBytes()
        diff.isNotEmpty().shouldBeTrue() // Sanity check the diff is not empty

        // Reset the file back to its old contents so that we can check the diff is applied
        fileToResolve.writeText("1")

        val file = FileSpec.newBuilder().apply {
            projectBuilder.repoRemote = "git@github.com:user/repo1.git"
            projectBuilder.revision = "master"
            projectBuilder.patchBuilder.patch = ByteString.copyFrom(diff)
            path = fileToResolve.name
        }.build()

        val gitFS = mockk<GitFS> {
            every { cloneRepo(file.project.repoRemote, file.project.revision) } returns IO.just(tempDir)
            every { getFilesInRepo(tempDir) } returns IO.just(setOf(fileToResolve))
        }

        val resolver = DefaultDependencyResolver(gitFS)
        val resolvedFile = resolver.resolve(file)
        resolvedFile.shouldBe(fileToResolve)
        resolvedFile.readText().shouldBe("2")

        verifyOrder {
            gitFS.cloneRepo(file.project.repoRemote, file.project.revision)
            gitFS.getFilesInRepo(tempDir)
        }
    }

    @Test
    fun `resolve a file added in a patch`(@TempDir tempDir: File) {
        // Init a git repo, add all files, and commit
        runAndPrintOutput(tempDir, "git", "init", ".")
        runAndPrintOutput(tempDir, "git", "add", ".")
        runAndPrintOutput(tempDir, "git", "commit", "-m", "a", "--allow-empty")

        // Create a new file
        val aNewFile = createTempFile(suffix = ".groovy", directory = tempDir)
        aNewFile.writeText("1")

        runAndPrintOutput(tempDir, "git", "add", aNewFile.name)
        val diff = run(tempDir, "git", "diff", "--cached").inputStream.readAllBytes()
        diff.isNotEmpty().shouldBeTrue() // Sanity check the diff is not empty

        // Delete the file so we can check the diff is applied
        aNewFile.delete().shouldBeTrue()

        val file = FileSpec.newBuilder().apply {
            projectBuilder.repoRemote = "git@github.com:user/repo1.git"
            projectBuilder.revision = "master"
            projectBuilder.patchBuilder.patch = ByteString.copyFrom(diff)
            path = aNewFile.name // Try to resolve that new file
        }.build()

        val gitFS = mockk<GitFS> {
            every { cloneRepo(file.project.repoRemote, file.project.revision) } returns IO.just(tempDir)

            // Simulate the diff being applied correctly by returning the new file. We check the diff worked later.
            every { getFilesInRepo(tempDir) } returns IO.just(setOf(File(aNewFile.path)))
        }

        val resolver = DefaultDependencyResolver(gitFS)
        val resolvedFile = resolver.resolve(file)
        resolvedFile.shouldBe(aNewFile)
        resolvedFile.readText().shouldBe("1") // Read from the new file to assert it was created properly

        verifyOrder {
            gitFS.cloneRepo(file.project.repoRemote, file.project.revision)
            gitFS.getFilesInRepo(tempDir)
        }
    }

    private fun run(dir: File, vararg cmd: String): Process =
        ProcessBuilder(*cmd).directory(dir).start().also {
            it.waitFor().shouldBeZero()
        }

    private fun runAndPrintOutput(dir: File, vararg cmd: String): Process =
        ProcessBuilder(*cmd).directory(dir).start().also {
            logger.debug { it.inputStream.readAllBytes().decodeToString() }
            logger.debug { it.errorStream.readAllBytes().decodeToString() }
            it.waitFor().shouldBeZero()
        }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
