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
import com.commonwealthrobotics.proto.script_host.CredentialsRequest
import com.commonwealthrobotics.proto.script_host.RunRequest
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import com.commonwealthrobotics.proto.script_host.TwoFactorRequest
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
                                msgBuilder.hasConfirmationRequest() -> msgBuilder.confirmationRequestBuilder.requestId = id
                                msgBuilder.hasCredentialsRequest() -> msgBuilder.credentialsRequestBuilder.requestId = id
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
                            else -> it.value.run {
                                logger.debug { "Received: $this" }
                                val requestId = when {
                                    hasConfirmationResponse() -> confirmationResponse.requestId
                                    hasCredentialsResponse() -> credentialsResponse.requestId
                                    hasTwoFactorResponse() -> twoFactorResponse.requestId
                                    hasError() -> error.requestId
                                    else -> null
                                }
                                when {
                                    requestId != null -> responseHandlers.remove(requestId)?.invoke(this)
                                    hasRunRequest() -> runRequest(runRequest, this@produce)
                                    hasCancelRequest() -> TODO("Not yet implemented")
                                    hasNewConfig() -> TODO("Not yet implemented")
                                }
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

        val script = out.withTask(runRequest.requestId, nextTaskID.getAndIncrement(), "Initializing ${runRequest.file.path}") {
            val script = scriptLoader.resolveAndLoad(runRequest.file, runRequest.devsList, runRequest.environmentMap)
            scriptRequestMap[runRequest.requestId] = script
            script
        }

        val result = out.withTask(runRequest.requestId, nextTaskID.getAndIncrement(), "Running ${runRequest.file.path}") {
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
        val msg = requests.call(SessionServerMessage.newBuilder().setCredentialsRequest(CredentialsRequest.newBuilder().setRemote(remote)))
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
            else -> throw RuntimeException("Unknown credentials response: $msg")
        }
    }

    override suspend fun getTwoFactorFor(remote: String): String {
        val msg = requests.call(SessionServerMessage.newBuilder().setTwoFactorRequest(TwoFactorRequest.newBuilder().setDescription(remote)))
        return when {
            msg.hasTwoFactorResponse() -> msg.twoFactorResponse.twoFactor
            msg.hasError() -> throw RuntimeException("2FA request error: " + msg.error.description)
            else -> throw RuntimeException("Unknown 2FA response: $msg")
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
