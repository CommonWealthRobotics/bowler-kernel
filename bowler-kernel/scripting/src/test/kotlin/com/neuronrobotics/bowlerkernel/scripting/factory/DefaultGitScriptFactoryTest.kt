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
import arrow.core.left
import arrow.core.right
import arrow.effects.IO
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.scripting.ScriptLanguage
import io.mockk.every
import io.mockk.mockk
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
        private val fakeUrl = "https://github.com/CommonWealthRobotics/FakeUrl.git"
        private val fakeFilename = "fakeFilename.kts"
        private val fakeGitFile = GitFile(gitUrl = fakeUrl, filename = fakeFilename)

        private lateinit var fakeFile: File
        private lateinit var factory: DefaultGitScriptFactory

        @BeforeEach
        fun beforeEach() {
            fakeFile = File(tempDir, fakeFilename).apply { writeText("42") }

            factory = DefaultGitScriptFactory(
                mockk {
                    every { cloneRepoAndGetFiles(fakeUrl) } returns IO.just(immutableListOf(fakeFile))
                },
                mockk {
                    every { parse("kts") } returns ScriptLanguage.Kotlin.right()
                }
            )
        }

        @Test
        fun `test with mock github api`() {
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
        fun `test using getInstanceFromGit`() {
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
        private val fakeUrl = "https://github.com/CommonWealthRobotics/FakeUrl.git"
        private val fakeFilename = "fakeFilename.unknownScriptLanguage"
        private val fakeGitFile = GitFile(gitUrl = fakeUrl, filename = fakeFilename)

        private lateinit var fakeFile: File
        private lateinit var factory: DefaultGitScriptFactory

        @BeforeEach
        fun beforeEach() {
            fakeFile = File(tempDir, fakeFilename).apply { writeText("42") }
            factory = DefaultGitScriptFactory(
                mockk {
                    every { cloneRepoAndGetFiles(fakeUrl) } returns IO.just(immutableListOf(fakeFile))
                },
                mockk {
                    every { parse(any()) } returns "".left()
                }
            )
        }

        @Test
        fun `test with unknown script language`() {
            val script = factory.createScriptFromGit(fakeGitFile).attempt().unsafeRunSync()

            assertTrue(script is Either.Left) {
                "Expected Either.Left, got: $script"
            }
        }

        @Test
        fun `test with unknown script language using getInstanceFromGit`() {
            val script = factory.createScriptFromGit(fakeGitFile).attempt().unsafeRunSync()

            assertTrue(script is Either.Left) {
                "Expected Either.Left, got: $script"
            }
        }
    }
}
