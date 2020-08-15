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

import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import io.grpc.stub.StreamObserver
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

/**
 * Careful ordering between these tests because of the global task ID counter.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class TaskUtilTest {

    @Test
    @Order(1)
    fun `run a task that does not throw an exception`() {
        val responseObserver = mockk< StreamObserver<SessionServerMessage>>(relaxUnitFun = true) {
        }

        val result = responseObserver.withTask(1, "desc") { 42 }
        result.shouldBeRight { it.shouldBe(42) }

        verifyOrder {
            responseObserver.onNext(sessionServerMessage(newTask = newTask(1, "desc", taskUpdate(0, Float.NaN))))
            responseObserver.onNext(sessionServerMessage(taskEnd = taskEnd(0)))
        }
    }

    @Test
    @Order(2)
    fun `run a task that throws a non-fatal exception`() {
        val responseObserver = mockk< StreamObserver<SessionServerMessage>>(relaxUnitFun = true) {
        }

        val result = responseObserver.withTask(1, "desc") { error("Boom!") }
        result.shouldBeLeft {
            it.shouldBeInstanceOf<IllegalStateException> { it.message.shouldBe("Boom!") }
        }

        verifyOrder {
            responseObserver.onNext(sessionServerMessage(newTask = newTask(1, "desc", taskUpdate(1, Float.NaN))))
            responseObserver.onNext(sessionServerMessage(requestError = requestError(1, "Boom!")))
        }
    }

    @Test
    @Order(3)
    fun `run a task that throws a fatal exception`() {
        val responseObserver = mockk< StreamObserver<SessionServerMessage>>(relaxUnitFun = true) {
        }

        shouldThrow<OutOfMemoryError> {
            responseObserver.withTask(1, "desc") { throw OutOfMemoryError() }
        }

        verifyOrder {
            responseObserver.onNext(sessionServerMessage(newTask = newTask(1, "desc", taskUpdate(2, Float.NaN))))
        }
    }
}
