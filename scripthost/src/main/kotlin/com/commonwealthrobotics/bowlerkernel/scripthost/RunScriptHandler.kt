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
import com.commonwealthrobotics.bowlerkernel.protoutil.withTask
import com.commonwealthrobotics.bowlerkernel.scripting.ScriptLoader
import com.commonwealthrobotics.proto.script_host.NewTask
import com.commonwealthrobotics.proto.script_host.RunRequest
import com.commonwealthrobotics.proto.script_host.RunResponse
import com.commonwealthrobotics.proto.script_host.TaskEnd
import com.commonwealthrobotics.proto.script_host.TaskUpdate
import com.google.protobuf.ByteString
import com.google.protobuf.MessageLite
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class RunScriptHandler(
    private val koinComponent: KoinComponent
) : KoinComponent by koinComponent {

    private val scriptLoader by inject<ScriptLoader>()

    // Session-scoped unique ID counters
    private val nextTaskID = AtomicLong(1)

    private fun createRunResponse(taskMsg: MessageLite): RunResponse = RunResponse.newBuilder().apply {
        when (taskMsg) {
            is NewTask -> newTask = taskMsg
            is TaskUpdate -> taskUpdate = taskMsg
            is TaskEnd -> taskEnd = taskMsg
            else -> require(false) { "Invalid task type: $taskMsg" }
        }
    }.build()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun runScript(request: RunRequest): Flow<RunResponse> {
        return channelFlow {
            logger.debug { "Starting run request:\n$request" }

            val script = withTask(
                nextTaskID.getAndIncrement(),
                "Initializing ${request.file.path}",
                this@RunScriptHandler::createRunResponse
            ) {
                logger.debug { "Loading script." }
                scriptLoader.resolveAndLoad(request.file, request.devsList, request.environmentMap)
            }

            logger.debug { "Script initialized." }

            val result = withTask(
                nextTaskID.getAndIncrement(),
                "Running ${request.file.path}",
                this@RunScriptHandler::createRunResponse
            ) {
                logger.debug { "Starting script." }

                // Establish redirections before starting the script so that all output is captured
                script.redirectOut {
                    launch {
                        send(
                            RunResponse.newBuilder().apply {
                                intermediateOutputBuilder.stdoutBytes = ByteString.copyFrom(it)
                            }.build()
                        )
                    }
                }

                script.redirectErr {
                    launch {
                        send(
                            RunResponse.newBuilder().apply {
                                intermediateOutputBuilder.stderrBytes = ByteString.copyFrom(it)
                            }.build()
                        )
                    }
                }

                script.start(emptyList(), null)
                val scriptResult = script.join(0, 1000, TimeUnit.MILLISECONDS)
                logger.info { "Script returned:\n$scriptResult" }
                scriptResult
            }

            when (result) {
                is Either.Left -> send(
                    RunResponse.newBuilder().apply {
                        errorBuilder.apply {
                            description = result.a.localizedMessage
                            stacktrace = result.a.stackTraceToString()
                        }
                    }.build()
                )

                is Either.Right -> send(
                    RunResponse.newBuilder().apply {
                        scriptOutputBuilder.apply {
                            output = result.b?.toString()
                            // TODO: Set stdout and stderr
                            // TODO: Set tarball
                        }
                    }.build()
                )
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
