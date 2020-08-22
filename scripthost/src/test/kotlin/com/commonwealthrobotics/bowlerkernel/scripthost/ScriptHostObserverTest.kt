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
import com.commonwealthrobotics.bowlerkernel.authservice.Credentials
import com.commonwealthrobotics.bowlerkernel.protoutil.fileSpec
import com.commonwealthrobotics.bowlerkernel.protoutil.newTask
import com.commonwealthrobotics.bowlerkernel.protoutil.patch
import com.commonwealthrobotics.bowlerkernel.protoutil.projectSpec
import com.commonwealthrobotics.bowlerkernel.protoutil.runRequest
import com.commonwealthrobotics.bowlerkernel.protoutil.sessionClientMessage
import com.commonwealthrobotics.bowlerkernel.protoutil.sessionServerMessage
import com.commonwealthrobotics.bowlerkernel.protoutil.taskEnd
import com.commonwealthrobotics.bowlerkernel.protoutil.taskUpdate
import com.commonwealthrobotics.bowlerkernel.scripting.Script
import com.commonwealthrobotics.bowlerkernel.scripting.ScriptLoader
import com.commonwealthrobotics.bowlerkernel.testutil.KoinTestFixture
import com.commonwealthrobotics.bowlerkernel.util.toChannel
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import com.commonwealthrobotics.proto.script_host.TaskEndCause
import io.grpc.stub.StreamObserver
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import kotlin.concurrent.thread

@OptIn(ExperimentalCoroutinesApi::class)
internal class ScriptHostObserverTest : KoinTestFixture() {

    @Test
    fun `a run request must trigger the script loader`() {
        val responseObserver = mockk< StreamObserver<SessionServerMessage>>(relaxUnitFun = true)
        // Have the script return nothing interesting
        val script = mockk<Script>(relaxUnitFun = true) {
            every { join(any(), any(), any()) } returns Either.Right(Unit)
        }
        val scriptLoader = mockk<ScriptLoader> {
            every { resolveAndLoad(any(), any(), any()) } returns script
        }

        testKoin(
            module {
                factory { scriptLoader }
            }
        )

        val scriptHost = ScriptHostObserver(responseObserver)
        val file = fileSpec(
            projectSpec("git@github.com:user/repo1.git", "master", patch(byteArrayOf())),
            "file1.groovy"
        )
        val devs = listOf(projectSpec("git@github.com:user/repo2.git", "master", patch(byteArrayOf())))
        val environment = mapOf("KEY" to "VALUE")
        scriptHost.onNext(sessionClientMessage(runRequest = runRequest(1, file, devs, environment)))

        verifyOrder {
            // Initialize the script
            responseObserver.onNext(
                sessionServerMessage(
                    newTask = newTask(1, "Initializing file1.groovy", taskUpdate(0, Float.NaN))
                )
            )
            scriptLoader.resolveAndLoad(file, devs, environment)
            responseObserver.onNext(sessionServerMessage(taskEnd = taskEnd(0, TaskEndCause.TASK_COMPLETED)))

            // Run the script
            responseObserver.onNext(
                sessionServerMessage(
                    newTask = newTask(1, "Running file1.groovy", taskUpdate(1, Float.NaN))
                )
            )
            script.start(emptyList(), null)
            script.join(any(), any(), any())
            responseObserver.onNext(sessionServerMessage(taskEnd = taskEnd(1, TaskEndCause.TASK_COMPLETED)))
        }
    }

    @Test
    fun `request credentials during script resolution`() {
        val remote = "git@github.com:user/repo1.git"

        val clientFlow = flowOf(
            SessionClientMessage.newBuilder().apply {
                credentialsResponseBuilder.apply {
                    requestId = 1
                    basicBuilder.apply {
                        username = "username"
                        password = "password"
                    }
                }
            }.build()
        ).toChannel()
        val session = ScriptSession(clientFlow)
        thread { runBlocking { session.sessionFlow.collect() } }
        runBlocking { session.getCredentialsFor(remote) } shouldBe Credentials.Basic("username", "password")
    }

    @Test
    fun `request 2fa during script resolution`() {
        val remote = "git@github.com:user/repo1.git"

        val clientFlow = flowOf(
            SessionClientMessage.newBuilder().apply {
                twoFactorResponseBuilder.apply {
                    requestId = 1
                    twoFactor = "token"
                }
            }.build()
        ).toChannel()
        val session = ScriptSession(clientFlow)
        thread { runBlocking { session.sessionFlow.collect() } }
        runBlocking { session.getTwoFactorFor(remote) } shouldBe "token"
    }
}
