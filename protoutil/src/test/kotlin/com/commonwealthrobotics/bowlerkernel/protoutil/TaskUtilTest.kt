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
package com.commonwealthrobotics.bowlerkernel.protoutil

import arrow.core.extensions.set.monoidal.identity
import com.commonwealthrobotics.proto.script_host.NewTask
import com.commonwealthrobotics.proto.script_host.TaskEnd
import com.commonwealthrobotics.proto.script_host.TaskEndCause
import com.google.protobuf.MessageLite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class TaskUtilTest {

    private fun <A> identity(a: A) = a

    @Test
    fun `run a task that does not throw an exception`() {
        val responseObserver = mockk<ProducerScope<MessageLite>>(relaxUnitFun = true)

        val result = runBlocking { responseObserver.withTask(1, "desc", this@TaskUtilTest::identity) { 42 } }
        result.shouldBe(42)

        coVerifyOrder {
            responseObserver.send(
                NewTask.newBuilder().apply {
                    description = "desc"
                    taskBuilder.apply {
                        taskId = 1
                        progress = Float.NaN
                    }
                }.build()
            )

            responseObserver.send(
                TaskEnd.newBuilder().apply {
                    taskId = 1
                    cause = TaskEndCause.TASK_COMPLETED
                }.build()
            )
        }
    }

    @Test
    fun `run a task that throws a non-fatal exception`() {
        val responseObserver = mockk<ProducerScope<MessageLite>>(relaxUnitFun = true)

        @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_AGAINST_NOT_NOTHING_EXPECTED_TYPE")
        val ex = shouldThrow<IllegalStateException> {
            runBlocking { responseObserver.withTask(1, "desc", this@TaskUtilTest::identity) { error("Boom!") } }
        }
        ex.message.shouldBe("Boom!")

        coVerifyOrder {
            responseObserver.send(
                NewTask.newBuilder().apply {
                    description = "desc"
                    taskBuilder.apply {
                        taskId = 1
                        progress = Float.NaN
                    }
                }.build()
            )

            responseObserver.send(
                TaskEnd.newBuilder().apply {
                    taskId = 1
                    cause = TaskEndCause.TASK_FAILED
                    error = "Boom!"
                }.build()
            )
        }
    }

    @Test
    fun `run a task that throws a fatal exception`() {
        val responseObserver = mockk<ProducerScope<MessageLite>>(relaxUnitFun = true)

        shouldThrow<OutOfMemoryError> {
            runBlocking {
                @Suppress("RemoveExplicitTypeArguments")
                responseObserver.withTask<Nothing, MessageLite>(
                    1,
                    "desc",
                    this@TaskUtilTest::identity
                ) { throw OutOfMemoryError() }
            }
        }

        coVerifyOrder {
            responseObserver.send(
                NewTask.newBuilder().apply {
                    description = "desc"
                    taskBuilder.apply {
                        taskId = 1
                        progress = Float.NaN
                    }
                }.build()
            )
        }
    }
}
