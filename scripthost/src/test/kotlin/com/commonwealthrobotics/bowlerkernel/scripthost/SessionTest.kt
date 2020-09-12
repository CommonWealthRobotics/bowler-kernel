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
import com.commonwealthrobotics.bowlerkernel.protoutil.sessionClientMessage
import com.commonwealthrobotics.bowlerkernel.protoutil.sessionServerMessage
import com.commonwealthrobotics.bowlerkernel.scripting.Script
import com.commonwealthrobotics.bowlerkernel.scripting.ScriptLoader
import com.commonwealthrobotics.bowlerkernel.testutil.KoinTestFixture
import com.commonwealthrobotics.bowlerkernel.util.KCountDownLatch
import com.commonwealthrobotics.bowlerkernel.util.toChannel
import com.commonwealthrobotics.proto.gitfs.ProjectSpec
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import com.commonwealthrobotics.proto.script_host.TaskEndCause
import com.google.protobuf.ByteString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.koin.dsl.module
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class SessionTest : KoinTestFixture() {

    companion object {
        private const val remote1 = "git@github.com:user/repo1.git"
        private const val remote2 = "git@github.com:user/repo2.git"
    }

    private val runScriptMsg = sessionClientMessage {
        runRequestBuilder.requestId = 1
        runRequestBuilder.fileBuilder.projectBuilder.repoRemote = "git@github.com:user/repo1.git"
        runRequestBuilder.fileBuilder.projectBuilder.revision = "master"
        runRequestBuilder.fileBuilder.projectBuilder.patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
        runRequestBuilder.fileBuilder.path = "file1.groovy"
        runRequestBuilder.addDevs(
            ProjectSpec.newBuilder().apply {
                repoRemote = "git@github.com:user/repo2.git"
                revision = "master"
                patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
            }
        )
        runRequestBuilder.putEnvironment("KEY", "VALUE")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun runSession(client: suspend SendChannel<SessionClientMessage>.(ReceiveChannel<SessionServerMessage>) -> Unit): Session {
        val channel = Channel<Flow<SessionServerMessage>>()
        val scope = CoroutineScope(Dispatchers.Default)

        val clientFlow = scope.produce {
            val serverFlow = channel.receive()
            client(serverFlow.toChannel(scope))
        }.consumeAsFlow()

        val session = Session(scope, clientFlow)
        runBlocking { channel.send(session.server) }
        return session
    }

    @Test
    fun `a run request must trigger the script loader`() {
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

        val client = flowOf(runScriptMsg)
        val session = Session(CoroutineScope(Dispatchers.Default), client)
        val responses = runBlocking { session.server.toList() }

        // Order should not be important because the task ID can be used by the client to determine ordering
        responses.shouldContainAll(
            sessionServerMessage {
                newTaskBuilder.requestId = 1
                newTaskBuilder.description = "Initializing file1.groovy"
                newTaskBuilder.taskBuilder.taskId = 1
                newTaskBuilder.taskBuilder.progress = Float.NaN
            },
            sessionServerMessage {
                taskEndBuilder.taskId = 1
                taskEndBuilder.cause = TaskEndCause.TASK_COMPLETED
            },
            sessionServerMessage {
                newTaskBuilder.requestId = 1
                newTaskBuilder.description = "Running file1.groovy"
                newTaskBuilder.taskBuilder.taskId = 2
                newTaskBuilder.taskBuilder.progress = Float.NaN
            },
            sessionServerMessage {
                taskEndBuilder.taskId = 2
                taskEndBuilder.cause = TaskEndCause.TASK_COMPLETED
            }
        )

        verifyOrder {
            // Initialize the script
            scriptLoader.resolveAndLoad(
                runScriptMsg.runRequest.file,
                runScriptMsg.runRequest.devsList,
                runScriptMsg.runRequest.environmentMap
            )

            // Run the script
            script.start(emptyList(), null)
            script.join(any(), any(), any())
        }
    }

    @Test
    fun `error running a script`() {
        // Have the script return nothing interesting
        val script = mockk<Script>(relaxUnitFun = true) {
            every { join(any(), any(), any()) } returns Either.Left(RuntimeException("Boom!"))
        }
        val scriptLoader = mockk<ScriptLoader> {
            every { resolveAndLoad(any(), any(), any()) } returns script
        }

        testKoin(
            module {
                factory { scriptLoader }
            }
        )

        val client = flowOf(runScriptMsg)
        val session = Session(CoroutineScope(Dispatchers.Default), client)
        val responses = runBlocking { session.server.toList() }

        // Order should not be important because the task ID can be used by the client to determine ordering
        responses.shouldContainAll(
            sessionServerMessage {
                newTaskBuilder.requestId = 1
                newTaskBuilder.description = "Initializing file1.groovy"
                newTaskBuilder.taskBuilder.taskId = 1
                newTaskBuilder.taskBuilder.progress = Float.NaN
            },
            sessionServerMessage {
                taskEndBuilder.taskId = 1
                taskEndBuilder.cause = TaskEndCause.TASK_COMPLETED
            },
            sessionServerMessage {
                newTaskBuilder.requestId = 1
                newTaskBuilder.description = "Running file1.groovy"
                newTaskBuilder.taskBuilder.taskId = 2
                newTaskBuilder.taskBuilder.progress = Float.NaN
            },
            sessionServerMessage {
                taskEndBuilder.taskId = 2
                taskEndBuilder.cause = TaskEndCause.TASK_FAILED
            }
        )
        // Error because the script returned an Either.Left
        responses.shouldExist {
            it.hasError() && it.error.requestId == 1L
        }

        verifyOrder {
            // Initialize the script
            scriptLoader.resolveAndLoad(
                runScriptMsg.runRequest.file,
                runScriptMsg.runRequest.devsList,
                runScriptMsg.runRequest.environmentMap
            )

            // Run the script
            script.start(emptyList(), null)
            script.join(any(), any(), any())
        }
    }

    @Test
    fun `request credentials`() {
        testKoin(module {})
        val session = runSession { server ->
            val id = server.receive().run {
                hasCredentialsRequest() shouldBe true
                credentialsRequest.remote shouldBe remote1
                credentialsRequest.requestId
            }
            send(sessionClientMessage {
                credentialsResponseBuilder.requestId = id
                credentialsResponseBuilder.basicBuilder.username = "username"
                credentialsResponseBuilder.basicBuilder.password = "password"
            })
        }
        runBlocking { session.getCredentialsFor(remote1) } shouldBe Credentials.Basic("username", "password")
    }

    @Test
    fun `error during credentials request`() {
        testKoin(module {})
        val session = runSession { server ->
            val id = server.receive().run {
                hasCredentialsRequest() shouldBe true
                credentialsRequest.remote shouldBe remote1
                credentialsRequest.requestId
            }
            send(sessionClientMessage {
                errorBuilder.requestId = id
                errorBuilder.description = "Boom!"
            })
        }
        runBlocking { shouldThrow<IllegalStateException> { session.getCredentialsFor(remote1) } }
    }

    @Test
    fun `request credentials during script resolution race condition`() {
        testKoin(module {})
        val latch = KCountDownLatch(1)
        val session = runSession { server ->
            val id1 = server.receive().run {
                hasCredentialsRequest() shouldBe true
                credentialsRequest.remote shouldBe remote1
                credentialsRequest.requestId
            }
            latch.countDown()
            val id2 = server.receive().run {
                hasCredentialsRequest() shouldBe true
                credentialsRequest.remote shouldBe remote2
                credentialsRequest.requestId
            }
            send(sessionClientMessage {
                credentialsResponseBuilder.requestId = id2
                credentialsResponseBuilder.basicBuilder.username = "username2"
                credentialsResponseBuilder.basicBuilder.password = "password2"
            })
            send(sessionClientMessage {
                credentialsResponseBuilder.requestId = id1
                credentialsResponseBuilder.basicBuilder.username = "username1"
                credentialsResponseBuilder.basicBuilder.password = "password1"
            })
        }
        runBlocking {
            awaitAll(
                    async { session.getCredentialsFor(remote1) },
                    async { latch.await(); session.getCredentialsFor(remote2) }
            ).shouldContainExactly(
                Credentials.Basic("username1", "password1"),
                Credentials.Basic("username2", "password2")
            )
        }
    }

    @Test
    fun `starting a session in GlobalScope is an error`() {
        testKoin(module {})
        shouldThrow<IllegalArgumentException> {
            Session(GlobalScope, flowOf())
        }
    }

    @Test
    fun `scripts must be able to run in parallel`() {
        val runScript1 = sessionClientMessage {
            runRequestBuilder.requestId = 1
            runRequestBuilder.fileBuilder.projectBuilder.repoRemote = "git@github.com:user/repo1.git"
            runRequestBuilder.fileBuilder.projectBuilder.revision = "master"
            runRequestBuilder.fileBuilder.projectBuilder.patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
            runRequestBuilder.fileBuilder.path = "file1.groovy"
        }
        val runScript2 = sessionClientMessage {
            runRequestBuilder.requestId = 2
            runRequestBuilder.fileBuilder.projectBuilder.repoRemote = "git@github.com:user/repo1.git"
            runRequestBuilder.fileBuilder.projectBuilder.revision = "master"
            runRequestBuilder.fileBuilder.projectBuilder.patchBuilder.patch = ByteString.copyFrom(byteArrayOf())
            runRequestBuilder.fileBuilder.path = "file2.groovy"
        }
        val client = flowOf(runScript1, runScript2)

        val scriptLatch = CountDownLatch(2)
        val script1 = mockk<Script>(relaxUnitFun = true) {
            every { join(any(), any(), any()) } answers {
                scriptLatch.countDown()
                scriptLatch.await()
                Either.Right(Unit)
            }
        }
        val script2 = mockk<Script>(relaxUnitFun = true) {
            every { join(any(), any(), any()) } answers {
                scriptLatch.countDown()
                scriptLatch.await()
                Either.Right(Unit)
            }
        }
        val scriptLoader = mockk<ScriptLoader> {
            every { resolveAndLoad(runScript1.runRequest.file, any(), any()) } returns script1
            every { resolveAndLoad(runScript2.runRequest.file, any(), any()) } returns script2
        }

        testKoin(
            module {
                factory { scriptLoader }
            }
        )

        val session = Session(CoroutineScope(Dispatchers.Default), client)
        runBlocking { session.server.collect() }
        scriptLatch.await()
    }
}
