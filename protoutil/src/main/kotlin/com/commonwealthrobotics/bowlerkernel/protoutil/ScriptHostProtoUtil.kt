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

import com.commonwealthrobotics.proto.gitfs.FileSpec
import com.commonwealthrobotics.proto.gitfs.ProjectSpec
import com.commonwealthrobotics.proto.script_host.ConfirmationResponse
import com.commonwealthrobotics.proto.script_host.CredentialsResponse
import com.commonwealthrobotics.proto.script_host.NewTask
import com.commonwealthrobotics.proto.script_host.RequestError
import com.commonwealthrobotics.proto.script_host.RunRequest
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import com.commonwealthrobotics.proto.script_host.TaskEnd
import com.commonwealthrobotics.proto.script_host.TaskEndCause
import com.commonwealthrobotics.proto.script_host.TaskUpdate
import com.commonwealthrobotics.proto.script_host.TwoFactorResponse

fun sessionClientMessage(
    runRequest: RunRequest? = null,
    confirmationResponse: ConfirmationResponse? = null,
    credentialsResponse: CredentialsResponse? = null,
    twoFactorResponse: TwoFactorResponse? = null,
    error: RequestError? = null
) = SessionClientMessage.newBuilder().apply {
    runRequest?.let { setRunRequest(runRequest) }
    confirmationResponse?.let { setConfirmationResponse(confirmationResponse) }
    credentialsResponse?.let { setCredentialsResponse(credentialsResponse) }
    twoFactorResponse?.let { setTwoFactorResponse(twoFactorResponse) }
    error?.let { setError(error) }
}.build()

fun runRequest(requestId: Long, file: FileSpec, devs: List<ProjectSpec>, environment: Map<String, String>) =
    RunRequest.newBuilder().apply {
        setRequestId(requestId)
        setFile(file)
        addAllDevs(devs)
        putAllEnvironment(environment)
    }.build()

fun sessionServerMessage(
    newTask: NewTask? = null,
    taskUpdate: TaskUpdate? = null,
    taskEnd: TaskEnd? = null,
    requestError: RequestError? = null
) = SessionServerMessage.newBuilder().apply {
    newTask?.let { setNewTask(newTask) }
    taskUpdate?.let { setTaskUpdate(taskUpdate) }
    taskEnd?.let { setTaskEnd(taskEnd) }
    requestError?.let { setError(requestError) }
}.build()

fun newTask(requestId: Long, description: String, task: TaskUpdate) = NewTask.newBuilder().apply {
    setRequestId(requestId)
    setDescription(description)
    setTask(task)
}.build()

fun taskUpdate(taskId: Long, progress: Number) = TaskUpdate.newBuilder().apply {
    setTaskId(taskId)
    setProgress(progress.toFloat())
}.build()

fun taskEnd(taskId: Long, cause: TaskEndCause) = TaskEnd.newBuilder().apply {
    setTaskId(taskId)
    setCause(cause)
}.build()

fun requestError(requestId: Long, description: String) = RequestError.newBuilder().apply {
    setRequestId(requestId)
    setDescription(description)
}.build()
