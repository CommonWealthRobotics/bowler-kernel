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
import arrow.fx.IO
import arrow.fx.extensions.io.monad.flatten
import com.commonwealthrobotics.bowlerkernel.authservice.Credentials
import com.commonwealthrobotics.bowlerkernel.authservice.CredentialsProvider
import com.commonwealthrobotics.bowlerkernel.di.GITHUB_CACHE_DIRECTORY_KOIN_NAME
import com.commonwealthrobotics.bowlerkernel.gitfs.GitFS
import com.commonwealthrobotics.bowlerkernel.gitfs.GitHubFS
import com.commonwealthrobotics.bowlerkernel.protoutil.sessionServerMessage
import com.commonwealthrobotics.bowlerkernel.protoutil.withTask
import com.commonwealthrobotics.bowlerkernel.scripting.Script
import com.commonwealthrobotics.bowlerkernel.scripting.ScriptLoader
import com.commonwealthrobotics.bowlerkernel.util.CallbackLatch
import com.commonwealthrobotics.bowlerkernel.util.toChannel
import com.commonwealthrobotics.proto.script_host.RunRequest
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import mu.KotlinLogging
import org.koin.core.KoinComponent
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.ExperimentalTime

/**
 * Implements the bidirectional session RPC.
 *
 * @param coroutineScope The scope used to process messages from the client flow. Cannot be GlobalScope.
 * @param client The client flow.
 */
class Session(
    private val coroutineScope: CoroutineScope,
    private val client: Flow<SessionClientMessage>,
    private val koinComponent: KoinComponent
) : CredentialsProvider, KoinComponent by koinComponent {

    // Bind a GitFS using this session so that credentials requests will go through this session
    private val modules by lazy {
        listOf(module { single<GitFS> { GitHubFS(this@Session, get(named(GITHUB_CACHE_DIRECTORY_KOIN_NAME))) } })
    }

    // Session-scoped unique ID counters
    private val nextRequest = AtomicLong(1)
    private val nextTaskID = AtomicLong(1)

    // Request-response handling
    private val responseHandlers: MutableMap<Long, (SessionClientMessage) -> Unit> = mutableMapOf()
    private val requests = CallbackLatch<SessionServerMessage.Builder, SessionClientMessage>()
    private val jobs = mutableListOf<Job>()
    private val scriptRequestMap = mutableMapOf<Long, Script>()

    /**
     * This is the main loop that handles a single session.
     */
    @OptIn(ExperimentalCoroutinesApi::class, InternalCoroutinesApi::class, ExperimentalTime::class)
    val server: Flow<SessionServerMessage> = coroutineScope.produce<SessionServerMessage> {
        val sessionChannel = this
        @Suppress("TooGenericExceptionCaught") // Okay because we just log and rethrow
        try {
            logger.debug { "Session started." }

            val clientChannel = client.toChannel(coroutineScope)
            do {
                val cont = select<Boolean> {
                    requests.onReceive {
                        logger.debug { "Got internal request: ${it.input} " }
                        if (isRequest(it.input)) {
                            // For requests, generate a new request ID and set it on the relevant builder
                            val id = nextRequest.getAndIncrement()

                            it.input.apply {
                                when {
                                    hasConfirmationRequest() -> confirmationRequestBuilder.requestId = id
                                    hasCredentialsRequest() -> credentialsRequestBuilder.requestId = id
                                }
                            }

                            send(it.input.build() as SessionServerMessage)

                            // Also need to set the response handler so the callback will be called
                            responseHandlers[id] = it.callback
                        } else {
                            send(it.input.build() as SessionServerMessage)
                        }

                        true
                    }

                    clientChannel.onReceiveOrClosed {
                        when {
                            it.isClosed -> {
                                logger.debug { "Client channel closed." }
                                false
                            }

                            else -> it.value.let { msg ->
                                logger.debug { "Received: $msg" }

                                val requestId = when {
                                    msg.hasConfirmationResponse() -> msg.confirmationResponse.requestId
                                    msg.hasCredentialsResponse() -> msg.credentialsResponse.requestId
                                    msg.hasError() -> msg.error.requestId
                                    else -> null
                                }

                                val resultThunk = when {
                                    requestId != null -> IO(coroutineScope.coroutineContext) {
                                        responseHandlers.remove(requestId)?.invoke(msg)
                                    }
                                    msg.hasRunRequest() -> runRequest(msg.runRequest, sessionChannel)
                                    msg.hasCancelRequest() -> TODO("Not yet implemented")
                                    msg.hasNewConfig() -> TODO("Not yet implemented")
                                    else -> throw IllegalStateException("Unhandled message type: $msg")
                                }

                                jobs.add(
                                    launch {
                                        val result = resultThunk.attempt().suspended()
                                        logger.debug { "Result of handling client message: $result" }

                                        if (result is Either.Left) {
                                            logger.warn(result.a) { "Request failed." }
                                            send(
                                                sessionServerMessage {
                                                    errorBuilder.requestId = msg.runRequest.requestId
                                                    errorBuilder.description =
                                                        "Request failed: ${result.a.localizedMessage}"
                                                }
                                            )
                                        }
                                    }
                                )

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
    }.consumeAsFlow().onCompletion {
        logger.debug { "Joining all Jobs. ${jobs.size} remaining." }
        jobs.joinAll()
        logger.debug { "Unloading Koin modules." }
        getKoin().unloadModules(modules)
        logger.debug { "Session ended." }
    }

    init {
        require(coroutineScope != GlobalScope) {
            "A session may not be started in GlobalScope."
        }

        @Suppress("TooGenericExceptionCaught") // Okay because we just log and rethrow
        try {
            getKoin().loadModules(modules)
        } catch (t: Throwable) {
            logger.error(t) { "Unhandled error when constructing session" }
            throw t
        }
    }

    /**
     * Handles a [RunRequest].
     *
     * @param runRequest The [RunRequest]
     * @param sessionChannel A channel to send messages to the client.
     * @return The result of loading and running the script.
     */
    private suspend fun runRequest(
        runRequest: RunRequest,
        sessionChannel: SendChannel<SessionServerMessage>
    ): IO<Any?> = IO {
        logger.debug { "requestId=${runRequest.requestId} Starting run request $runRequest" }
        val scriptLoader = getKoin().get<ScriptLoader>()
        logger.debug { "requestId=${runRequest.requestId} Got script loader $scriptLoader" }

        val script = sessionChannel.withTask(
            runRequest.requestId,
            nextTaskID.getAndIncrement(),
            "Initializing ${runRequest.file.path}",
            coroutineScope.coroutineContext
        ) {
            logger.debug { "requestId=${runRequest.requestId} Loading script." }
            val script = scriptLoader.resolveAndLoad(runRequest.file, runRequest.devsList, runRequest.environmentMap)
            scriptRequestMap[runRequest.requestId] = script
            script
        }.suspended()

        logger.debug { "requestId=${runRequest.requestId} Script initialized." }

        sessionChannel.withTask(
            runRequest.requestId,
            nextTaskID.getAndIncrement(),
            "Running ${runRequest.file.path}",
            coroutineScope.coroutineContext
        ) {
            logger.debug { "requestId=${runRequest.requestId} Starting script." }
            script.start(emptyList(), null)
            val scriptResult = script.join(0, 1000, TimeUnit.MILLISECONDS)
            logger.info { "requestId=${runRequest.requestId} Script returned:\n$scriptResult" }
            when (scriptResult) {
                // Throw when the script errored so that the task and request fail
                is Either.Left -> throw scriptResult.a
                is Either.Right -> scriptResult.b
            }
        }
    }.flatten()

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

    /**
     * @return true if the [builder] is a "request" type (meaning it has a request ID).
     */
    private fun isRequest(builder: SessionServerMessage.Builder) =
        builder.hasConfirmationRequest() || builder.hasCredentialsRequest()

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
