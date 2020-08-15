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

import com.commonwealthrobotics.bowlerkernel.protoutil.fileSpec
import com.commonwealthrobotics.bowlerkernel.protoutil.patch
import com.commonwealthrobotics.bowlerkernel.protoutil.projectSpec
import com.commonwealthrobotics.bowlerkernel.protoutil.runRequest
import com.commonwealthrobotics.bowlerkernel.protoutil.sessionClientMessage
import com.commonwealthrobotics.proto.script_host.ScriptHostGrpc
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import com.commonwealthrobotics.proto.script_host.TaskEndCause
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import io.kotest.matchers.collections.shouldExist
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class KernelServerIntegTest {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    @Test
    fun `run test script`() {
        val server = KernelServer()
        server.start()

        val channel = ManagedChannelBuilder.forAddress("localhost", server.port).usePlaintext().build()
        val stub = ScriptHostGrpc.newStub(channel)
        val received = mutableListOf<SessionServerMessage>()
        val latch = CountDownLatch(4)
        val session = stub.session(object : StreamObserver<SessionServerMessage> {
            override fun onNext(value: SessionServerMessage) {
                logger.debug { "$value" }
                received.add(value)
                latch.countDown()
            }

            override fun onError(t: Throwable) {
                throw t
            }

            @SuppressWarnings("EmptyFunctionBlock")
            override fun onCompleted() {
            }
        })

        session.onNext(
            sessionClientMessage(
                runRequest = runRequest(
                    1,
                    fileSpec(
                        projectSpec(
                            "https://github.com/CommonWealthRobotics/bowler-kernel-test-repo.git",
                            "master",
                            patch(byteArrayOf())
                        ),
                        "scriptA.groovy"
                    ),
                    listOf(),
                    mapOf()
                )
            )
        )

        session.onCompleted()
        latch.await()

        received.shouldExist { it.hasNewTask() && it.newTask.description == "Running scriptA.groovy" }
        received.shouldExist { it.hasTaskEnd() && it.taskEnd.cause == TaskEndCause.TASK_COMPLETED }

        server.stop()
    }
}
