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
package com.commonwealthrobotics.bowlerkernel.scripting

import arrow.core.Either
import com.commonwealthrobotics.proto.gitfs.FileSpec
import com.google.protobuf.ByteString
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * This class is meant to test classpath isolation between scripts. There are two primary requirements:
 * 1. A dependency of one script (added via Grapes) must not be visible to other scripts. This means that if a script A
 *      has a dependency on class Foo, another script B must not be able to load class Foo without also declaring a
 *      dependency on it.
 * 2. Following from (1), two scripts must be able to declare different versions of the same dependency without
 *      conflict. This means that if a script A has a dependency on version 1.0 of class Foo, another script B must be
 *      able to declare a dependency on a different version of Foo and load that version (not A's version of Foo).
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class ScriptClasspathIsolationIntegTest {

    @Test
    fun `requirement 1`(@TempDir tempDir: File) {
        // We create one script that grabs and loads a dependency (and returns an instance of it to ensure that grapes
        // work).
        val scriptWithDepFile = createTempFile(suffix = ".groovy", directory = tempDir)
        scriptWithDepFile.writeText(
            """
            @Grapes(@Grab(group = 'commons-cli', module = 'commons-cli', version = '1.4'))
            import org.apache.commons.cli.Options;
            return new Options()
            """.trimIndent()
        )

        val scriptWithDepFS = FileSpec.newBuilder().apply {
            projectBuilder.repoRemote = "https://github.com/a/b.git"
            projectBuilder.revision = "master"
            projectBuilder.patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
            path = "scriptWithDep.groovy"
        }.build()

        // We create another script that tries to access that same dependency.
        val scriptWithoutDepFile = createTempFile(suffix = ".groovy", directory = tempDir)
        scriptWithoutDepFile.writeText(
            """
            import org.apache.commons.cli.Options;
            return new Options()
            """.trimIndent()
        )

        val scriptWithoutDepFS = FileSpec.newBuilder().apply {
            projectBuilder.repoRemote = "https://github.com/a/b.git"
            projectBuilder.revision = "master"
            projectBuilder.patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
            path = "scriptWithoutDep.groovy"
        }.build()

        val loader = DefaultScriptLoader(
            mockk(relaxUnitFun = true) {
                coEvery { resolve(scriptWithDepFS) } returns scriptWithDepFile
                coEvery { resolve(scriptWithoutDepFS) } returns scriptWithoutDepFile
            }
        )

        runBlocking {
            val scriptWithDep = loader.resolveAndLoad(scriptWithDepFS, listOf(), mapOf())
            val scriptWithoutDep = loader.resolveAndLoad(scriptWithoutDepFS, listOf(), mapOf())

            scriptWithDep.start(listOf(), null)
            scriptWithoutDep.start(listOf(), null)

            // The script that declared the dependency must have access to it.
            scriptWithDep.join().shouldBeRight {
                it.shouldNotBeNull()
                it::class.java.name.shouldBe("org.apache.commons.cli.Options")
            }

            // The script that did not declare the dependency must not have access to it.
            scriptWithoutDep.join().shouldBeLeft {
                it.message.shouldContain("unable to resolve class org.apache.commons.cli.Options")
            }
        }
    }

    @Test
    fun `transitivity of requirement 1`(@TempDir tempDir: File) {
        // We create a parent script that loads a dep and starts a child script
        val parentScriptFile = createTempFile(suffix = ".groovy", directory = tempDir)
        parentScriptFile.writeText(
            """
            @Grapes(@Grab(group = 'commons-cli', module = 'commons-cli', version = '1.4'))
            import com.commonwealthrobotics.bowlerkernel.scripting.ScriptExecutionEnvironment;
            
            return scriptExecutionEnvironment.startChildScript(args[0], [:], []).join()
            """.trimIndent()
        )

        val parentScriptFS = FileSpec.newBuilder().apply {
            projectBuilder.repoRemote = "https://github.com/a/b.git"
            projectBuilder.revision = "master"
            projectBuilder.patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
            path = "parentScript.groovy"
        }.build()

        // We create a child script that loads the same dep without grabbing it first
        val childScriptFile = createTempFile(suffix = ".groovy", directory = tempDir)
        childScriptFile.writeText(
            """
            import org.apache.commons.cli.Options;
            return new Options()
            """.trimIndent()
        )

        val childScriptFS = FileSpec.newBuilder().apply {
            projectBuilder.repoRemote = "https://github.com/a/b.git"
            projectBuilder.revision = "master"
            projectBuilder.patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
            path = "childScript.groovy"
        }.build()

        val loader = DefaultScriptLoader(
            mockk(relaxUnitFun = true) {
                coEvery { resolve(parentScriptFS) } returns parentScriptFile
                coEvery { resolve(childScriptFS) } returns childScriptFile
            }
        )

        runBlocking {
            val parentScript = loader.resolveAndLoad(parentScriptFS, listOf(), mapOf())
            parentScript.start(listOf(childScriptFS), null)

            // The child script must not have access to the dependency from the parent script.
            parentScript.join().shouldBeRight {
                it.shouldBeInstanceOf<Either.Left<Throwable>>()
                it.value.message.shouldContain("unable to resolve class org.apache.commons.cli.Options")
            }
        }
    }

    @Test
    fun `requirement 2`(@TempDir tempDir: File) {
        // We create one script that grabs a newer version of a dependency
        val scriptWithNewerDepFile = createTempFile(suffix = ".groovy", directory = tempDir)
        scriptWithNewerDepFile.writeText(
            """
            @Grapes(@Grab(group = 'commons-cli', module = 'commons-cli', version = '1.4'))
            import org.apache.commons.cli.Option;
            import org.apache.commons.cli.Option.Builder;
            return Option.Builder
            """.trimIndent()
        )

        val scriptWithNewerDepFS = FileSpec.newBuilder().apply {
            projectBuilder.repoRemote = "https://github.com/a/b.git"
            projectBuilder.revision = "master"
            projectBuilder.patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
            path = "scriptWithNewerDep.groovy"
        }.build()

        // We create another script that grabs an older version of the same dependency and tries to import something
        // from the newer version of that dependency
        val scriptWithOlderDepFile = createTempFile(suffix = ".groovy", directory = tempDir)
        scriptWithOlderDepFile.writeText(
            """
            @Grapes(@Grab(group = 'commons-cli', module = 'commons-cli', version = '1.1'))
            import org.apache.commons.cli.Option;
            import org.apache.commons.cli.Option.Builder;
            return Option.Builder
            """.trimIndent()
        )

        val scriptWithOlderDepFS = FileSpec.newBuilder().apply {
            projectBuilder.repoRemote = "https://github.com/a/b.git"
            projectBuilder.revision = "master"
            projectBuilder.patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
            path = "scriptWithOlderDep.groovy"
        }.build()

        val loader = DefaultScriptLoader(
            mockk(relaxUnitFun = true) {
                coEvery { resolve(scriptWithNewerDepFS) } returns scriptWithNewerDepFile
                coEvery { resolve(scriptWithOlderDepFS) } returns scriptWithOlderDepFile
            }
        )

        runBlocking {
            val scriptWithNewerDep = loader.resolveAndLoad(scriptWithNewerDepFS, listOf(), mapOf())
            val scriptWithOlderDep = loader.resolveAndLoad(scriptWithOlderDepFS, listOf(), mapOf())

            scriptWithNewerDep.start(listOf(), null)
            scriptWithOlderDep.start(listOf(), null)

            // The script that declared the newer dependency must succeed
            scriptWithNewerDep.join().shouldBeRight { it.shouldNotBeNull() }

            // The script that declared the older dependency must fail
            scriptWithOlderDep.join().shouldBeLeft {
                it.message.shouldContain("unable to resolve class org.apache.commons.cli.Option.Builder")
            }
        }
    }
}
