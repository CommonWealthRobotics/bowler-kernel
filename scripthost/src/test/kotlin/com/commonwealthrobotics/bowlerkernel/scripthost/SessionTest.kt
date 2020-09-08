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
import com.commonwealthrobotics.bowlerkernel.protoutil.*
import com.commonwealthrobotics.bowlerkernel.scripting.Script
import com.commonwealthrobotics.bowlerkernel.scripting.ScriptLoader
import com.commonwealthrobotics.bowlerkernel.testutil.KoinTestFixture
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.TaskEndCause
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import org.junit.platform.commons.annotation.Testable
import org.koin.dsl.module
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@Timeout(value = 30, unit = TimeUnit.SECONDS)
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

        val file = fileSpec(
                projectSpec("git@github.com:user/repo1.git", "master", patch(byteArrayOf())),
                "file1.groovy"
        )
        val devs = listOf(projectSpec("git@github.com:user/repo2.git", "master", patch(byteArrayOf())))
        val environment = mapOf("KEY" to "VALUE")
        val client = flowOf(sessionClientMessage(runRequest = runRequest(1, file, devs, environment)))
        val session = Session(CoroutineScope(Dispatchers.Default), client)
        val responses = runBlocking { session.session.toList() }

        // Order should not be important because the task ID can be used by the client to determine ordering
        responses.shouldContainAll(
                sessionServerMessage(newTask = newTask(1, "Initializing file1.groovy", taskUpdate(1, Float.NaN))),
                sessionServerMessage(taskEnd = taskEnd(1, TaskEndCause.TASK_COMPLETED)),
                sessionServerMessage(newTask = newTask(1, "Running file1.groovy", taskUpdate(2, Float.NaN))),
                sessionServerMessage(taskEnd = taskEnd(2, TaskEndCause.TASK_COMPLETED))
        )

        verifyOrder {
            // Initialize the script
            scriptLoader.resolveAndLoad(file, devs, environment)

            // Run the script
            script.start(emptyList(), null)
            script.join(any(), any(), any())
        }
    }

    @Test
    fun `request credentials during script resolution`() {
        val client = flow {
            while (true) {
                delay(100)
                emit(SessionClientMessage.newBuilder().apply {
                    credentialsResponseBuilder.apply {
                        requestId = 1
                        basicBuilder.apply {
                            username = "username"
                            password = "password"
                        }
                    }
                }.build())
            }
        }
        val session = Session(CoroutineScope(Dispatchers.Default), client)
        thread { runBlocking { session.session.collect() } }
        runBlocking { session.getCredentialsFor(remote1) } shouldBe Credentials.Basic("username", "password")
    }

    @Test
    fun `error during credentials request during script resolution`() {
        val client = flow {
            while (true) {
                delay(100)
                emit(SessionClientMessage.newBuilder().apply {
                    errorBuilder.apply {
                        requestId = 1
                        description = "Boom!"
                    }
                }.build())
            }
        }
        val session = Session(CoroutineScope(Dispatchers.Default), client)
        thread { runBlocking { session.session.collect() } }
        runBlocking { shouldThrow<IllegalStateException> { session.getCredentialsFor(remote1) } }
    }

    @Test
    fun `request 2fa during script resolution`() {
        val client = flow {
            while (true) {
                delay(100)
                emit(SessionClientMessage.newBuilder().apply {
                    twoFactorResponseBuilder.apply {
                        requestId = 1
                        twoFactor = "token"
                    }
                }.build())
            }
        }
        val session = Session(CoroutineScope(Dispatchers.Default), client)
        thread { runBlocking { session.session.collect() } }
        runBlocking { session.getTwoFactorFor(remote1) } shouldBe "token"
    }

    @Test
    fun `request credentials during script resolution race condition`() {
        val client = flow {
            while (true) {
                delay(100)
                emit(SessionClientMessage.newBuilder().apply {
                    credentialsResponseBuilder.apply {
                        requestId = 2
                        basicBuilder.apply {
                            username = "username2"
                            password = "password2"
                        }
                    }
                }.build())
                emit(SessionClientMessage.newBuilder().apply {
                    credentialsResponseBuilder.apply {
                        requestId = 1
                        basicBuilder.apply {
                            username = "username1"
                            password = "password1"
                        }
                    }
                }.build())
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
}
