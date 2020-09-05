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

import com.commonwealthrobotics.bowlerkernel.authservice.Credentials
import com.commonwealthrobotics.bowlerkernel.authservice.CredentialsProvider
import com.commonwealthrobotics.bowlerkernel.util.toChannel
import com.commonwealthrobotics.proto.script_host.ScriptHostGrpc
import com.commonwealthrobotics.proto.script_host.ScriptHostGrpcKt
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.selects.select
import mu.KotlinLogging
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ScriptHost(
        private val scope: CoroutineScope
) : ScriptHostGrpcKt.ScriptHostCoroutineImplBase() {

    override fun session(requests: Flow<SessionClientMessage>): Flow<SessionServerMessage> {
        return Session(scope, requests).session
    }
}

class Session(
        private val scope: CoroutineScope,
        private val client: Flow<SessionClientMessage>
) : CredentialsProvider {
    private data class CredentialsTransaction(val remote: String, val handler: suspend (SessionClientMessage) -> Unit)
    private data class TwoFactorTransaction(val remote: String, val handler: suspend (SessionClientMessage) -> Unit)

    private val credentialsTransactions = Channel<CredentialsTransaction>()
    private val twoFactorTransactions = Channel<TwoFactorTransaction>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val session = flow {
        val clientChannel = client.toChannel(scope)
        var nextRequest: Long = 0
        val requestHandlers: MutableMap<Long, suspend (SessionClientMessage) -> Unit> = mutableMapOf()
        while (!clientChannel.isClosedForReceive) {
            select<Unit> {
                credentialsTransactions.onReceive {
                    val msg = SessionServerMessage.newBuilder()
                    val req = msg.credentialsRequestBuilder
                    req.requestId = nextRequest++
                    req.remote = it.remote
                    emit(msg.build())
                    requestHandlers[req.requestId] = it.handler
                }
                twoFactorTransactions.onReceive {
                    val msg = SessionServerMessage.newBuilder()
                    val req = msg.twoFactorRequestBuilder
                    req.requestId = nextRequest++
                    req.description = it.remote
                    emit(msg.build())
                    requestHandlers[req.requestId] = it.handler
                }
                clientChannel.onReceive {
                    val requestId = when {
                        it.hasConfirmationResponse() -> it.confirmationResponse.requestId
                        it.hasCredentialsResponse() -> it.credentialsResponse.requestId
                        it.hasTwoFactorResponse() -> it.twoFactorResponse.requestId
                        it.hasError() -> it.error.requestId
                        else -> null
                    }
                    if (requestId != null) {
                        requestHandlers.remove(requestId)?.invoke(it)
                    } else {
                        when {
                            it.hasRunRequest() -> TODO("Not yet implemented")
                            it.hasCancelRequest() -> TODO("Not yet implemented")
                            it.hasNewConfig() -> TODO("Not yet implemented")
                        }
                    }
                }
            }
        }
    }

    init {
        GlobalScope.launch {
            client.filter { it.hasCancelRequest() }.map {
                logger.debug { "Cancel request: $it" }
            }.collect()
        }
    }

    override suspend fun getCredentialsFor(remote: String): Credentials {
        val msg = suspendCoroutine<SessionClientMessage> { cont ->
            credentialsTransactions.sendBlocking(CredentialsTransaction(remote) { cont.resume(it) })
        }
        return when {
            msg.hasCredentialsResponse() -> {
                val res = msg.credentialsResponse
                when {
                    res.hasBasic() -> Credentials.Basic(res.basic.username, res.basic.password)
                    res.hasOauth() -> Credentials.OAuth(res.oauth.token)
                    else -> Credentials.Anonymous
                }
            }
            msg.hasError() -> throw RuntimeException("Credentials request error: " + msg.error.description)
            else -> throw RuntimeException("Unknown credentials response: $msg")
        }
    }

    override suspend fun getTwoFactorFor(remote: String): String {
        val msg = suspendCoroutine<SessionClientMessage> { cont ->
            twoFactorTransactions.sendBlocking(TwoFactorTransaction(remote) { cont.resume(it) })
        }
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
