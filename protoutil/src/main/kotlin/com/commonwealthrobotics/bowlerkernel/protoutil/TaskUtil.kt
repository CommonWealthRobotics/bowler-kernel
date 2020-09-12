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

import arrow.core.nonFatalOrThrow
import arrow.fx.IO
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import com.commonwealthrobotics.proto.script_host.TaskEndCause
import kotlinx.coroutines.channels.SendChannel
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private object TaskUtil {
    val logger = KotlinLogging.logger { }
}

/**
 * Runs [f] in the context of a new task. The new task responds to the request with id [requestId] and has a
 * [description]. The task will start with an indeterminate progress.
 *
 * @param requestId The ID of the request this task was created from.
 * @param taskId The ID of this task.
 * @param description A short description of the task.
 * @param ctx The [CoroutineContext] used to dispatch the returned [IO].
 * @param f The function to execute in the context of the task.
 * @return The return value of [f].
 */
@SuppressWarnings("TooGenericExceptionCaught")
suspend fun <T> SendChannel<SessionServerMessage>.withTask(
    requestId: Long,
    taskId: Long,
    description: String,
    ctx: CoroutineContext = EmptyCoroutineContext,
    f: suspend () -> T
): IO<T> = IO(ctx) {
    send(
        sessionServerMessage {
            newTaskBuilder.requestId = requestId
            newTaskBuilder.description = description
            newTaskBuilder.taskBuilder.taskId = taskId
            newTaskBuilder.taskBuilder.progress = Float.NaN
        }
    )

    val out = try {
        f()
    } catch (ex: Throwable) {
        TaskUtil.logger.error(ex) {
            "Error running the script during request $requestId"
        }

        // Handle non-fatal exceptions by erroring the request
        val nonFatal = ex.nonFatalOrThrow()
        send(
            sessionServerMessage {
                taskEndBuilder.taskId = taskId
                taskEndBuilder.cause = TaskEndCause.TASK_FAILED
            }
        )
        send(
            sessionServerMessage {
                errorBuilder.requestId = requestId
                errorBuilder.description = nonFatal.message ?: "Unknown exception message."
            }
        )

        // Exit early to avoid sending a TaskEnd in addition to a RequestError
        throw ex
    }

    send(
        sessionServerMessage {
            taskEndBuilder.taskId = taskId
            taskEndBuilder.cause = TaskEndCause.TASK_COMPLETED
        }
    )

    out
}
