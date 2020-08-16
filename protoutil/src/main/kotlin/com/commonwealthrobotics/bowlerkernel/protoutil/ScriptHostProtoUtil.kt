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
@file:SuppressWarnings("TooManyFunctions")

package com.commonwealthrobotics.bowlerkernel.protoutil

import com.commonwealthrobotics.proto.gitfs.FileSpec
import com.commonwealthrobotics.proto.gitfs.ProjectSpec
import com.commonwealthrobotics.proto.robot_config.RobotConfig
import com.commonwealthrobotics.proto.script_host.BasicCredentials
import com.commonwealthrobotics.proto.script_host.ConfirmationRequest
import com.commonwealthrobotics.proto.script_host.ConfirmationResponse
import com.commonwealthrobotics.proto.script_host.CredentialsRequest
import com.commonwealthrobotics.proto.script_host.CredentialsResponse
import com.commonwealthrobotics.proto.script_host.NewConfig
import com.commonwealthrobotics.proto.script_host.NewTask
import com.commonwealthrobotics.proto.script_host.OAuthCredentials
import com.commonwealthrobotics.proto.script_host.RequestError
import com.commonwealthrobotics.proto.script_host.RunRequest
import com.commonwealthrobotics.proto.script_host.ScriptOutput
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import com.commonwealthrobotics.proto.script_host.TaskEnd
import com.commonwealthrobotics.proto.script_host.TaskEndCause
import com.commonwealthrobotics.proto.script_host.TaskUpdate
import com.commonwealthrobotics.proto.script_host.TwoFactorRequest
import com.commonwealthrobotics.proto.script_host.TwoFactorResponse
import com.google.protobuf.ByteString

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

fun credentialsResponse(requestId: Long, basic: BasicCredentials? = null, oauth: OAuthCredentials? = null) =
    CredentialsResponse.newBuilder().apply {
        setRequestId(requestId)
        basic?.let { setBasic(basic) }
        oauth?.let { setOauth(oauth) }
    }.build()

fun basicCredentials(username: String, password: String) =
    BasicCredentials.newBuilder().apply {
        setUsername(username)
        setPassword(password)
    }.build()

fun oauthCredentials(token: String) =
    OAuthCredentials.newBuilder().apply {
        setToken(token)
    }.build()

fun twoFactorResponse(requestId: Long, twoFactor: String) =
    TwoFactorResponse.newBuilder().apply {
        setRequestId(requestId)
        setTwoFactor(twoFactor)
    }.build()

fun sessionServerMessage(
    newTask: NewTask? = null,
    taskUpdate: TaskUpdate? = null,
    taskEnd: TaskEnd? = null,
    confirmationRequest: ConfirmationRequest? = null,
    credentialsRequest: CredentialsRequest? = null,
    twoFactorRequest: TwoFactorRequest? = null,
    scriptOutput: ScriptOutput? = null,
    newConfig: NewConfig? = null,
    requestError: RequestError? = null
) = SessionServerMessage.newBuilder().apply {
    newTask?.let { setNewTask(newTask) }
    taskUpdate?.let { setTaskUpdate(taskUpdate) }
    taskEnd?.let { setTaskEnd(taskEnd) }
    confirmationRequest?.let { setConfirmationRequest(confirmationRequest) }
    credentialsRequest?.let { setCredentialsRequest(credentialsRequest) }
    twoFactorRequest?.let { setTwoFactorRequest(twoFactorRequest) }
    scriptOutput?.let { setScriptOutput(scriptOutput) }
    newConfig?.let { setNewConfig(newConfig) }
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

fun confirmationRequest(requestId: Long, description: String) = ConfirmationRequest.newBuilder().apply {
    setRequestId(requestId)
    setDescription(description)
}.build()

fun credentialsRequest(requestId: Long, taskId: Long, remote: String) = CredentialsRequest.newBuilder().apply {
    setRequestId(requestId)
    setTaskId(taskId)
    setRemote(remote)
}.build()

fun twoFactorRequest(requestId: Long, description: String) = TwoFactorRequest.newBuilder().apply {
    setRequestId(requestId)
    setDescription(description)
}.build()

fun scriptOutput(tarball: ByteArray) = ScriptOutput.newBuilder().apply {
    setTarball(ByteString.copyFrom(tarball))
}.build()

fun newConfig(config: RobotConfig) = NewConfig.newBuilder().apply {
    setConfig(config)
}.build()

fun requestError(requestId: Long, description: String) = RequestError.newBuilder().apply {
    setRequestId(requestId)
    setDescription(description)
}.build()
