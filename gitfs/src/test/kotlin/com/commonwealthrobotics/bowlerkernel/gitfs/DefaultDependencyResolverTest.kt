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

import com.commonwealthrobotics.bowlerkernel.util.run
import com.commonwealthrobotics.bowlerkernel.util.runAndPrintOutput
import com.commonwealthrobotics.proto.gitfs.FileSpec
import com.google.protobuf.ByteString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempFile

@OptIn(ExperimentalPathApi::class)
internal class DefaultDependencyResolverTest {

    @Test
    fun `resolve a script with no patch and no devs`(@TempDir tempDir: File) {
        val fileToResolve = createTempFile(tempDir.toPath(), suffix = ".groovy").toFile()
        val file = FileSpec.newBuilder().apply {
            projectBuilder.repoRemote = "git@github.com:user/repo1.git"
            projectBuilder.revision = "master"
            projectBuilder.patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
            path = fileToResolve.name
        }.build()

        val gitFS = mockk<GitFS> {
            coEvery { cloneRepo(file.project.repoRemote, file.project.revision) } returns tempDir
            coEvery { getFilesInRepo(tempDir) } returns setOf(
                createTempFile(tempDir.toPath(), suffix = ".groovy").toFile(),
                fileToResolve,
                createTempFile(tempDir.toPath(), suffix = ".groovy").toFile()
            )
        }

        runBlocking {
            val resolver = DefaultDependencyResolver(gitFS)
            val resolvedFile = resolver.resolve(file)
            resolvedFile.shouldBe(fileToResolve)
        }

        coVerifyOrder {
            gitFS.cloneRepo(file.project.repoRemote, file.project.revision)
            gitFS.getFilesInRepo(tempDir)
        }
    }

    @Test
    fun `fail to resolve because cloning fails`(@TempDir tempDir: File) {
        val file = FileSpec.newBuilder().apply {
            projectBuilder.repoRemote = "git@github.com:user/repo1.git"
            projectBuilder.revision = "master"
            projectBuilder.patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
            path = "tmp.groovy"
        }.build()

        val gitFS = mockk<GitFS> {
            coEvery {
                cloneRepo(file.project.repoRemote, file.project.revision)
            } throws UnsupportedOperationException("Not allowed to authenticate to the remote.")
        }

        runBlocking {
            val resolver = DefaultDependencyResolver(gitFS)
            shouldThrow<UnsupportedOperationException> {
                resolver.resolve(file)
            }
        }

        coVerifyOrder {
            gitFS.cloneRepo(file.project.repoRemote, file.project.revision)
        }
        coVerifyOrder(inverse = true) {
            gitFS.getFilesInRepo(tempDir)
        }
    }

    @Test
    fun `resolve a script with a patch and no devs`(@TempDir tempDir: File) {
        // Make a file and write 1 into it
        val fileToResolve = createTempFile(tempDir.toPath(), suffix = ".groovy").toFile()
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
            coEvery { cloneRepo(file.project.repoRemote, file.project.revision) } returns tempDir
            every { getFilesInRepo(tempDir) } returns setOf(fileToResolve)
        }

        runBlocking {
            val resolver = DefaultDependencyResolver(gitFS)
            val resolvedFile = resolver.resolve(file)
            resolvedFile.shouldBe(fileToResolve)
            resolvedFile.readText().shouldBe("2")
        }

        coVerifyOrder {
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
        val aNewFile = createTempFile(tempDir.toPath(), suffix = ".groovy").toFile()
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
            coEvery { cloneRepo(file.project.repoRemote, file.project.revision) } returns tempDir

            // Simulate the diff being applied correctly by returning the new file. We check the diff worked later.
            every { getFilesInRepo(tempDir) } returns setOf(File(aNewFile.path))
        }

        runBlocking {
            val resolver = DefaultDependencyResolver(gitFS)
            val resolvedFile = resolver.resolve(file)
            resolvedFile.shouldBe(aNewFile)
            resolvedFile.readText().shouldBe("1") // Read from the new file to assert it was created properly
        }

        coVerifyOrder {
            gitFS.cloneRepo(file.project.repoRemote, file.project.revision)
            gitFS.getFilesInRepo(tempDir)
        }
    }
}
