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

import arrow.core.Either
import arrow.core.nonFatalOrThrow
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import com.commonwealthrobotics.proto.script_host.TaskEndCause
import io.grpc.stub.StreamObserver
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicLong

private object TaskUtil {
    val taskIdCounter = AtomicLong(0)
    val logger = KotlinLogging.logger { }
}

/**
 * Runs [f] in the context of a new task. The new task responds to the request with id [requestId] and has a
 * [description]. The task will start with an indeterminate progress.
 *
 * @param requestId The id of the request this task was created from.
 * @param description A short description of the task.
 * @param f The function to execute in the context of the task.
 * @return The return value of the function or an error. Non-fatal exceptions are caught an wrapped in [Either.Left].
 */
fun <T> StreamObserver<SessionServerMessage>.withTask(
    requestId: Long,
    description: String,
    f: () -> T
): Either<Throwable, T> {
    val taskId = nextTaskId()
    onNext(sessionServerMessage(newTask = newTask(requestId, description, taskUpdate(taskId, Float.NaN))))

    val out: T
    try {
        out = f()
    } catch (ex: Throwable) {
        TaskUtil.logger.error(ex) {
            "Error running the script during request $requestId"
        }

        // Handle non-fatal exceptions by erroring the request
        val nonFatal = ex.nonFatalOrThrow()
        onNext(sessionServerMessage(taskEnd = taskEnd(taskId, TaskEndCause.TASK_FAILED)))
        onNext(
            sessionServerMessage(
                requestError =
                    requestError(requestId, nonFatal.message ?: "Unknown exception message.")
            )
        )

        // Exit early to avoid sending a TaskEnd in addition to a RequestError
        return Either.Left(ex)
    }

    onNext(sessionServerMessage(taskEnd = taskEnd(taskId, TaskEndCause.TASK_COMPLETED)))
    return Either.Right(out)
}

private fun nextTaskId() = TaskUtil.taskIdCounter.getAndIncrement()
