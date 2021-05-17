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
package com.commonwealthrobotics.bowlerkernel.scripthost

import arrow.core.Either
import com.commonwealthrobotics.bowlerkernel.scripting.Script
import com.commonwealthrobotics.bowlerkernel.scripting.ScriptLoader
import com.commonwealthrobotics.bowlerkernel.testutil.KoinTestFixture
import com.commonwealthrobotics.proto.gitfs.ProjectSpec
import com.commonwealthrobotics.proto.script_host.RunRequest
import com.commonwealthrobotics.proto.script_host.TaskEndCause
import com.google.protobuf.ByteString
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import java.lang.RuntimeException

internal class RunScriptHandlerTest : KoinTestFixture() {

    @Test
    fun `run script without errors`() {
        val script = mockk<Script>(relaxUnitFun = true) {
            // Returning a String from the script should result in that String being put directly into the script output
            // message.
            every { join(any(), any(), any()) } returns Either.Right("Script output")
        }
        val scriptLoader = mockk<ScriptLoader> {
            coEvery { resolveAndLoad(any(), any(), any()) } returns script
        }

        initKoin(
            module {
                factory { scriptLoader }
            }
        )

        val handler = RunScriptHandler(testLocalKoin)
        val responses = runBlocking { handler.runScript(request).toCollection(mutableListOf()) }

        responses[0].should { it.newTask.task.taskId.shouldBe(1) }
        responses[1].should {
            it.taskEnd.taskId.shouldBe(1)
            it.taskEnd.cause.shouldBe(TaskEndCause.TASK_COMPLETED)
        }
        responses[2].should { it.newTask.task.taskId.shouldBe(2) }
        responses[3].should {
            it.taskEnd.taskId.shouldBe(2)
            it.taskEnd.cause.shouldBe(TaskEndCause.TASK_COMPLETED)
        }
        responses[4].should { it.scriptOutput.output.shouldBe("Script output") }

        coVerifyOrder {
            scriptLoader.resolveAndLoad(request.file, request.devsList, request.environmentMap)
            script.start(emptyList(), null)
            script.join(any(), any(), any())
        }
    }

    @Test
    fun `run a script that terminates with an uncaught non-fatal exception`() {
        val script = mockk<Script>(relaxUnitFun = true) {
            // Returning a String from the script should result in that String being put directly into the script output
            // message.
            every { join(any(), any(), any()) } returns Either.Left(RuntimeException("Boom!"))
        }
        val scriptLoader = mockk<ScriptLoader> {
            coEvery { resolveAndLoad(any(), any(), any()) } returns script
        }

        initKoin(
            module {
                factory { scriptLoader }
            }
        )

        val handler = RunScriptHandler(testLocalKoin)
        val responses = runBlocking { handler.runScript(request).toCollection(mutableListOf()) }

        responses[0].should { it.newTask.task.taskId.shouldBe(1) }
        responses[1].should {
            it.taskEnd.taskId.shouldBe(1)
            it.taskEnd.cause.shouldBe(TaskEndCause.TASK_COMPLETED)
        }
        responses[2].should { it.newTask.task.taskId.shouldBe(2) }
        responses[3].should {
            it.taskEnd.taskId.shouldBe(2)
            it.taskEnd.cause.shouldBe(TaskEndCause.TASK_COMPLETED)
        }
        responses[4].should {
            it.error.description.shouldBe("Boom!")
            it.error.stacktrace.shouldNotBeEmpty()
        }

        coVerifyOrder {
            scriptLoader.resolveAndLoad(request.file, request.devsList, request.environmentMap)
            script.start(emptyList(), null)
            script.join(any(), any(), any())
        }
    }

    companion object {
        private val request = RunRequest.newBuilder().apply {
            fileBuilder.projectBuilder.repoRemote = "git@github.com:user/repo1.git"
            fileBuilder.projectBuilder.revision = "master"
            fileBuilder.projectBuilder.patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
            fileBuilder.path = "file1.groovy"
            addDevs(
                ProjectSpec.newBuilder().apply {
                    repoRemote = "git@github.com:user/repo2.git"
                    revision = "master"
                    patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
                }
            )
            putEnvironment("KEY", "VALUE")
        }.build()
    }
}
