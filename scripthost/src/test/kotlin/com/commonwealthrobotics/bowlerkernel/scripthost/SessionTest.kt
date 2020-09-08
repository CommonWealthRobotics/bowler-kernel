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
import com.commonwealthrobotics.proto.gitfs.ProjectSpec
import com.commonwealthrobotics.proto.script_host.TaskEndCause
import com.google.protobuf.ByteString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.koin.dsl.module
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@Timeout(value = 30, unit = TimeUnit.SECONDS)
@Execution(ExecutionMode.SAME_THREAD)
internal class SessionTest : KoinTestFixture() {

    companion object {
        private const val remote1 = "git@github.com:user/repo1.git"
        private const val remote2 = "git@github.com:user/repo2.git"
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

        val msg = sessionClientMessage {
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

        val client = flowOf(msg)
        val session = Session(CoroutineScope(Dispatchers.Default), client)
        val responses = runBlocking { session.session.toList() }

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
            scriptLoader.resolveAndLoad(msg.runRequest.file, msg.runRequest.devsList, msg.runRequest.environmentMap)

            // Run the script
            script.start(emptyList(), null)
            script.join(any(), any(), any())
        }
    }

    @Test
    fun `request credentials during script resolution`() {
        testKoin(module {})
        val client = flow {
            while (true) {
                delay(100)
                emit(
                    sessionClientMessage {
                        credentialsResponseBuilder.requestId = 1
                        credentialsResponseBuilder.basicBuilder.username = "username"
                        credentialsResponseBuilder.basicBuilder.password = "password"
                    }
                )
            }
        }
        val session = Session(CoroutineScope(Dispatchers.Default), client)
        thread { runBlocking { session.session.collect() } }
        runBlocking { session.getCredentialsFor(remote1) } shouldBe Credentials.Basic("username", "password")
    }

    @Test
    fun `error during credentials request during script resolution`() {
        testKoin(module {})
        val client = flow {
            while (true) {
                delay(100)
                emit(
                    sessionClientMessage {
                        errorBuilder.requestId = 1
                        errorBuilder.description = "Boom!"
                    }
                )
            }
        }
        val session = Session(CoroutineScope(Dispatchers.Default), client)
        thread { runBlocking { session.session.collect() } }
        runBlocking { shouldThrow<IllegalStateException> { session.getCredentialsFor(remote1) } }
    }

    @Test
    fun `request 2fa during script resolution`() {
        testKoin(module {})
        val client = flow {
            while (true) {
                delay(100)
                emit(
                    sessionClientMessage {
                        twoFactorResponseBuilder.requestId = 1
                        twoFactorResponseBuilder.twoFactor = "token"
                    }
                )
            }
        }
        val session = Session(CoroutineScope(Dispatchers.Default), client)
        thread { runBlocking { session.session.collect() } }
        runBlocking { session.getTwoFactorFor(remote1) } shouldBe "token"
    }

    @Test
    fun `request credentials during script resolution race condition`() {
        testKoin(module {})
        val client = flow {
            while (true) {
                delay(100)
                emit(
                    sessionClientMessage {
                        credentialsResponseBuilder.requestId = 2
                        credentialsResponseBuilder.basicBuilder.username = "username2"
                        credentialsResponseBuilder.basicBuilder.password = "password2"
                    }
                )
                emit(
                    sessionClientMessage {
                        credentialsResponseBuilder.requestId = 1
                        credentialsResponseBuilder.basicBuilder.username = "username1"
                        credentialsResponseBuilder.basicBuilder.password = "password1"
                    }
                )
            }
        }
        val session = Session(CoroutineScope(Dispatchers.Default), client)
        thread { runBlocking { session.session.collect() } }
        runBlocking {
            listOf(session.getCredentialsFor(remote1), session.getCredentialsFor(remote2)).shouldContainExactly(
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
}
