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
package com.commonwealthrobotics.bowlerkernel.server

import com.commonwealthrobotics.proto.policy.PolicyContainer
import com.commonwealthrobotics.proto.policy.PolicyDocument
import com.commonwealthrobotics.proto.policy.PolicyServiceGrpcKt
import com.commonwealthrobotics.proto.script_host.RunRequest
import com.commonwealthrobotics.proto.script_host.RunResponse
import com.commonwealthrobotics.proto.script_host.ScriptHostGrpcKt
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.TimeUnit

@Timeout(value = 5, unit = TimeUnit.MINUTES)
internal class KernelServerIntegTest {

    @Test
    fun `run test script`(@TempDir tempDir: File) {
        val server = KernelServer()
        server.port.shouldBe(-1)
        server.ensureStarted(gitHubCacheDirectory = tempDir.toPath())
        server.port.shouldBeGreaterThanOrEqual(0)

        val channel = ManagedChannelBuilder.forAddress("localhost", server.port)
            .usePlaintext()
            .build()
        val policyService = PolicyServiceGrpcKt.PolicyServiceCoroutineStub(channel)
        val scriptHost = ScriptHostGrpcKt.ScriptHostCoroutineStub(channel)

        val policy = PolicyDocument.newBuilder().apply {
            version = "2021-04-17"
            addStatements(
                PolicyContainer.newBuilder().apply {
                    credentialPolicyBuilder.credentials = "anonymous"
                    credentialPolicyBuilder.addScopes("github:repo:CommonWealthRobotics/bowler-kernel-test-repo")
                }.build()
            )
        }.build()

        val runRequest = RunRequest.newBuilder().apply {
            fileBuilder.projectBuilder.repoRemote =
                "https://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git"
            fileBuilder.projectBuilder.revision = "master"
            fileBuilder.projectBuilder.patchBuilder.patch = ByteString.EMPTY
            fileBuilder.path = "scriptA.groovy"
        }.build()

        runBlocking {
            policyService.setPolicyDocument(policy).accepted.shouldBeTrue()
        }

        repeat(10) {
            val result = runBlocking {
                scriptHost.runScript(runRequest).toCollection(mutableListOf())
            }

            result[0].shouldHaveNewTask(1, Float.NaN)
            result[1].shouldHaveTaskEnd(1)
            result[2].shouldHaveNewTask(2, Float.NaN)
            result.subList(3, result.size).shouldHaveSingleElement {
                it.hasTaskEnd() && it.taskEnd.taskId == 2L
            }
            result.subList(3, result.size).filter { it.hasIntermediateOutput() }.shouldHaveAtLeastSize(1)
            result.subList(3, result.size).shouldHaveSingleElement {
                it.hasScriptOutput() && it.scriptOutput.output == "42"
            }
        }

        server.ensureStopped()
        server.port.shouldBe(-1)
    }

    @Test
    fun `run script that tries to clone without creds`(@TempDir tempDir: File) {
        val server = KernelServer()
        server.port.shouldBe(-1)
        server.ensureStarted(gitHubCacheDirectory = tempDir.toPath())
        server.port.shouldBeGreaterThanOrEqual(0)

        val channel = ManagedChannelBuilder.forAddress("localhost", server.port)
            .usePlaintext()
            .build()
        val scriptHost = ScriptHostGrpcKt.ScriptHostCoroutineStub(channel)

        val runRequest = RunRequest.newBuilder().apply {
            fileBuilder.projectBuilder.repoRemote =
                "https://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git"
            fileBuilder.projectBuilder.revision = "master"
            fileBuilder.projectBuilder.patchBuilder.patch = ByteString.EMPTY
            fileBuilder.path = "scriptA.groovy"
        }.build()

        // Don't set a policy document before running this script so that nothing is allowed
        runBlocking {
            // This should throw because the policy document has not been set, so the kernel must act as if an empty
            // policy document was set, which denies all authentication operations
            shouldThrow<StatusException> {
                scriptHost.runScript(runRequest).toCollection(mutableListOf())
            }
        }

        server.ensureStopped()
        server.port.shouldBe(-1)
    }

    companion object {
        private fun RunResponse.shouldHaveNewTask(taskId: Long, progress: Float) {
            hasNewTask().shouldBeTrue()
            newTask.hasTask().shouldBeTrue()
            newTask.task.taskId.shouldBe(taskId)
            newTask.task.progress.shouldBe(progress)
        }

        private fun RunResponse.shouldHaveTaskEnd(taskId: Long) {
            hasTaskEnd().shouldBeTrue()
            taskEnd.taskId.shouldBe(taskId)
        }
    }
}
