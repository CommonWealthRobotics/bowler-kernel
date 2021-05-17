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
import com.commonwealthrobotics.proto.script_host.NewTask
import com.commonwealthrobotics.proto.script_host.TaskEnd
import com.commonwealthrobotics.proto.script_host.TaskEndCause
import com.commonwealthrobotics.proto.script_host.TaskUpdate
import com.google.protobuf.MessageLite
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import mu.KotlinLogging

private object TaskUtil {
    val logger = KotlinLogging.logger { }
}

/**
 * Runs [f] in the context of a new task. The task will start with an indeterminate progress. [f] may emit its own
 * [TaskUpdate] messages to send progress updates.
 *
 * @param newTaskID The ID of this task.
 * @param taskDescription A short description of the task.
 * @param createRequest Used to create the flow's request type from [NewTask], [TaskUpdate], and [TaskEnd] messages.
 * @param f The function to execute in the context of the task.
 * @return The return value of [f].
 */
@ExperimentalCoroutinesApi
@SuppressWarnings("TooGenericExceptionCaught")
suspend fun <T, O> ProducerScope<O>.withTask(
    newTaskID: Long,
    taskDescription: String,
    createRequest: (MessageLite) -> O,
    f: suspend ProducerScope<O>.() -> T
): T {
    send(
        createRequest(
            NewTask.newBuilder().apply {
                description = taskDescription
                taskBuilder.apply {
                    taskId = newTaskID
                    progress = Float.NaN
                }
            }.build()
        )
    )

    val out = try {
        f()
    } catch (ex: Throwable) {
        TaskUtil.logger.error(ex) {
            "Error running the script during request"
        }

        // Handle non-fatal exceptions by erroring the request
        val nonFatal = ex.nonFatalOrThrow()
        send(
            createRequest(
                TaskEnd.newBuilder().apply {
                    taskId = newTaskID
                    cause = TaskEndCause.TASK_FAILED
                    error = nonFatal.message ?: "Unknown exception message."
                }.build()
            )
        )

        // Exit early to avoid sending a TaskEnd in addition to a RequestError
        throw ex
    }

    send(
        createRequest(
            TaskEnd.newBuilder().apply {
                taskId = newTaskID
                cause = TaskEndCause.TASK_COMPLETED
            }.build()
        )
    )

    return out
}
