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
import arrow.core.extensions.either.monad.flatten
import arrow.core.flatMap
import com.commonwealthrobotics.bowlerkernel.authservice.Credentials
import com.commonwealthrobotics.bowlerkernel.authservice.CredentialsProvider
import com.commonwealthrobotics.bowlerkernel.di.BowlerKernelKoinComponent
import com.commonwealthrobotics.bowlerkernel.gitfs.GitFS
import com.commonwealthrobotics.bowlerkernel.gitfs.GitHubFS
import com.commonwealthrobotics.bowlerkernel.protoutil.withTask
import com.commonwealthrobotics.bowlerkernel.scripting.Script
import com.commonwealthrobotics.bowlerkernel.scripting.ScriptLoader
import com.commonwealthrobotics.bowlerkernel.util.CallbackLatch
import com.commonwealthrobotics.bowlerkernel.util.toChannel
import com.commonwealthrobotics.proto.script_host.RunRequest
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import com.google.protobuf.GeneratedMessageV3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.selects.select
import mu.KotlinLogging
import org.koin.core.get
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicLong

class Session(
    private val scope: CoroutineScope,
    private val client: Flow<SessionClientMessage>
) : CredentialsProvider, BowlerKernelKoinComponent {

    private val nextRequest = AtomicLong(1)
    private val nextTaskID = AtomicLong(1)
    private val responseHandlers: MutableMap<Long, (SessionClientMessage) -> Unit> = mutableMapOf()
    private val requests = CallbackLatch<GeneratedMessageV3.Builder<*>, SessionClientMessage>()
    private val scriptRequestMap = mutableMapOf<Long, Script>()

    private fun isRequest(builder: GeneratedMessageV3.Builder<*>) =
        (builder as SessionServerMessage.Builder).let {
            builder.hasConfirmationRequest() ||
                builder.hasCredentialsRequest() ||
                builder.hasTwoFactorRequest()
        }

    @OptIn(ExperimentalCoroutinesApi::class, InternalCoroutinesApi::class)
    val session: Flow<SessionServerMessage> = scope.produce {
        try {
            logger.debug { "Session started" }
            val clientChannel = client.toChannel(scope)
            do {
                val cont = select<Boolean> {
                    requests.onReceive {
                        if (isRequest(it.input)) {
                            val id = nextRequest.getAndIncrement()

                            val msgBuilder = it.input as SessionServerMessage.Builder
                            when {
                                msgBuilder.hasConfirmationRequest() ->
                                    msgBuilder.confirmationRequestBuilder.requestId = id
                                msgBuilder.hasCredentialsRequest() ->
                                    msgBuilder.credentialsRequestBuilder.requestId = id
                                msgBuilder.hasTwoFactorRequest() -> msgBuilder.twoFactorRequestBuilder.requestId = id
                            }

                            send(it.input.build() as SessionServerMessage)
                            responseHandlers[id] = it.callback
                        } else {
                            send(it.input.build() as SessionServerMessage)
                        }

                        true
                    }

                    clientChannel.onReceiveOrClosed {
                        when {
                            it.isClosed -> false

                            else -> it.value.let { msg ->
                                logger.debug { "Received: $msg" }

                                val requestId = when {
                                    msg.hasConfirmationResponse() -> msg.confirmationResponse.requestId
                                    msg.hasCredentialsResponse() -> msg.credentialsResponse.requestId
                                    msg.hasTwoFactorResponse() -> msg.twoFactorResponse.requestId
                                    msg.hasError() -> msg.error.requestId
                                    else -> null
                                }

                                val result = when {
                                    requestId != null -> {
                                        responseHandlers.remove(requestId)?.invoke(msg)
                                        Either.Right(Unit)
                                    }

                                    msg.hasRunRequest() -> runRequest(msg.runRequest, this@produce)
                                    msg.hasCancelRequest() -> TODO("Not yet implemented")
                                    msg.hasNewConfig() -> TODO("Not yet implemented")
                                    else -> throw IllegalStateException("Unhandled message type: $msg")
                                }

                                logger.debug { "Result of handling client message: $result" }

                                true
                            }
                        }
                    }
                }
            } while (cont)
        } catch (t: Throwable) {
            logger.error(t) { "Unhandled error in session" }
            throw t
        }
    }.consumeAsFlow()

    private suspend fun runRequest(
        runRequest: RunRequest,
        out: SendChannel<SessionServerMessage>
    ): Either<Throwable, Any?> {
        val modules = listOf(
            module {
                single<GitFS> { GitHubFS(this@Session) }
            }
        )
        getKoin().loadModules(modules)

        val scriptLoader = get<ScriptLoader>()

        val script = out.withTask(
            runRequest.requestId,
            nextTaskID.getAndIncrement(),
            "Initializing ${runRequest.file.path}"
        ) {
            val script = scriptLoader.resolveAndLoad(runRequest.file, runRequest.devsList, runRequest.environmentMap)
            scriptRequestMap[runRequest.requestId] = script
            script
        }

        val result = out.withTask(
            runRequest.requestId,
            nextTaskID.getAndIncrement(),
            "Running ${runRequest.file.path}"
        ) {
            script.flatMap {
                it.start(emptyList(), null)
                val scriptResult = it.join()
                logger.info { "Script returned:\n$scriptResult" }
                scriptResult
            }
        }.flatten()

        getKoin().unloadModules(modules)

        return result
    }

    override suspend fun getCredentialsFor(remote: String): Credentials {
        val msg = requests.call(
            SessionServerMessage.newBuilder().apply {
                credentialsRequestBuilder.remote = remote
            }
        )

        return when {
            msg.hasCredentialsResponse() -> {
                val res = msg.credentialsResponse
                when {
                    res.hasBasic() -> Credentials.Basic(res.basic.username, res.basic.password)
                    res.hasOauth() -> Credentials.OAuth(res.oauth.token)
                    else -> Credentials.Anonymous
                }
            }
            msg.hasError() -> throw IllegalStateException("Credentials request error: " + msg.error.description)
            else -> throw IllegalStateException("Unknown credentials response: $msg")
        }
    }

    override suspend fun getTwoFactorFor(remote: String): String {
        val msg = requests.call(
            SessionServerMessage.newBuilder().apply {
                twoFactorRequestBuilder.description = remote
            }
        )

        return when {
            msg.hasTwoFactorResponse() -> msg.twoFactorResponse.twoFactor
            msg.hasError() -> throw IllegalStateException("2FA request error: " + msg.error.description)
            else -> throw IllegalStateException("Unknown 2FA response: $msg")
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
