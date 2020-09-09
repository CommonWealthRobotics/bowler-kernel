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
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
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
}
