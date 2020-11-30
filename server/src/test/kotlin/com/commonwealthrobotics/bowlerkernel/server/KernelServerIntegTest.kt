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

import com.commonwealthrobotics.bowlerkernel.protoutil.sessionClientMessage
import com.commonwealthrobotics.bowlerkernel.scripthost.runSession
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import com.commonwealthrobotics.proto.script_host.TaskEndCause
import com.google.protobuf.ByteString
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Timeout(value = 5, unit = TimeUnit.MINUTES)
internal class KernelServerIntegTest {

    @Test
    fun `run test script`(@TempDir tempDir: File) {
        val server = KernelServer()
        server.port.shouldBe(-1)
        server.ensureStarted(gitHubCacheDirectory = tempDir.toPath())
        server.port.shouldBeGreaterThanOrEqual(0)

        val received = mutableListOf<SessionServerMessage>()
        val latch = CountDownLatch(4)
        runSession(server.koinComponent) { serverFlow ->
            send(
                sessionClientMessage {
                    runRequestBuilder.requestId = 1
                    runRequestBuilder.fileBuilder.projectBuilder.repoRemote =
                        "https://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git"
                    runRequestBuilder.fileBuilder.projectBuilder.revision = "master"
                    runRequestBuilder.fileBuilder.projectBuilder.patchBuilder.patch = ByteString.copyFrom(
                        byteArrayOf()
                    )
                    runRequestBuilder.fileBuilder.path = "scriptA.groovy"
                }
            )

            serverFlow.consumeAsFlow().collect {
                when {
                    it.hasCredentialsRequest() -> send(
                        sessionClientMessage {
                            credentialsResponseBuilder.requestId = it.credentialsRequest.requestId
                            credentialsResponseBuilder
                        }
                    )
                    it.hasNewTask() || it.hasTaskEnd() -> {
                        received.add(it)
                        latch.countDown()
                    }
                }
            }
        }
        latch.await()

        received.shouldExist { it.hasNewTask() && it.newTask.description == "Running scriptA.groovy" }
        received.shouldExist { it.hasTaskEnd() && it.taskEnd.cause == TaskEndCause.TASK_COMPLETED }

        server.ensureStopped()
        server.port.shouldBe(-1)
    }
}
