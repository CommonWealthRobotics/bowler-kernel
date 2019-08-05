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
package com.neuronrobotics.bowlerkernel.scripting.factory

import arrow.core.Either
import arrow.core.right
import arrow.effects.IO
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.scripting.parser.DefaultScriptLanguageParser
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf
import java.io.File
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultGitScriptFactoryTest {

    @Nested
    inner class TestsWithCorrectFilename {

        @TempDir
        lateinit var tempDir: File
        val fakeUrl = "https://github.com/CommonWealthRobotics/FakeUrl.git"
        val fakeFilename = "fakeFilename.kts"
        val fakeGitFile = GitFile(gitUrl = fakeUrl, filename = fakeFilename)
        lateinit var fakeFile: File

        lateinit var factory: DefaultGitScriptFactory

        @BeforeEach
        fun beforeEach() {
            fakeFile = File(tempDir, fakeFilename).apply { writeText("42") }
            factory = DefaultGitScriptFactory(
                mock {
                    on { cloneRepoAndGetFiles(fakeUrl) } doReturn IO.just(immutableListOf(fakeFile))
                },
                DefaultScriptLanguageParser()
            )
        }

        @Test
        fun `test with mock github api`(@TempDir tempDir: File) {
            val script = factory.createScriptFromGit(fakeGitFile).attempt().unsafeRunSync()

            assertTrue(script is Either.Right) {
                "Expected Either.Right, got: $script"
            }

            script as Either.Right
            script.b.apply {
                val result = startScript(emptyImmutableList())
                stopAndCleanUp()
                assertEquals(42.right(), result)
            }
        }

        @Test
        fun `test using getInstanceFromGit`(@TempDir tempDir: File) {
            val script = factory.getInstanceFromGit<Int>(fakeGitFile).attempt().unsafeRunSync()

            assertEquals(42.right().right(), script) {
                "Got: $script"
            }
        }
    }

    @Nested
    inner class TestsWithUnknownExtension {

        @TempDir
        lateinit var tempDir: File
        val fakeUrl = "https://github.com/CommonWealthRobotics/FakeUrl.git"
        val fakeFilename = "fakeFilename.unknownScriptLanguage"
        val fakeGitFile = GitFile(gitUrl = fakeUrl, filename = fakeFilename)
        lateinit var fakeFile: File

        lateinit var factory: DefaultGitScriptFactory

        @BeforeEach
        fun beforeEach() {
            fakeFile = File(tempDir, fakeFilename).apply { writeText("42") }
            factory = DefaultGitScriptFactory(
                mock {
                    on { cloneRepoAndGetFiles(fakeUrl) } doReturn IO.just(immutableListOf(fakeFile))
                },
                DefaultScriptLanguageParser()
            )
        }

        @Test
        fun `test with unknown script language`(@TempDir tempDir: File) {
            val script = factory.createScriptFromGit(fakeGitFile).attempt().unsafeRunSync()

            assertTrue(script is Either.Left) {
                "Expected Either.Left, got: $script"
            }
        }

        @Test
        fun `test with unknown script language using getInstanceFromGit`(@TempDir tempDir: File) {
            val script = factory.createScriptFromGit(fakeGitFile).attempt().unsafeRunSync()

            assertTrue(script is Either.Left) {
                "Expected Either.Left, got: $script"
            }
        }
    }
}
