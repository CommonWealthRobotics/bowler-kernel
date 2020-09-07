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
import com.commonwealthrobotics.bowlerkernel.util.CallbackLatch
import com.commonwealthrobotics.bowlerkernel.util.toChannel
import com.commonwealthrobotics.proto.script_host.ScriptHostGrpcKt
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.selects.select
import mu.KotlinLogging

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
    private val credentials = CallbackLatch<String, SessionClientMessage>()
    private val twoFactor = CallbackLatch<String, SessionClientMessage>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val session: Flow<SessionServerMessage> = flow {
        try {
            logger.debug { "Session started" }
            val clientChannel = client.toChannel(scope)
            mediate {
                do {
                    select<Unit> {
                        handleRequest(credentials) { id, input ->
                            credentialsRequestBuilder.apply {
                                requestId = id
                                remote = input
                            }
                        }
                        handleRequest(twoFactor) { id, input ->
                            twoFactorRequestBuilder.apply {
                                requestId = id
                                description = input
                            }
                        }
                        clientChannel.onReceive {
                            logger.debug { "Received: $it" }
                            val requestId = when {
                                it.hasConfirmationResponse() -> it.confirmationResponse.requestId
                                it.hasCredentialsResponse() -> it.credentialsResponse.requestId
                                it.hasTwoFactorResponse() -> it.twoFactorResponse.requestId
                                it.hasError() -> it.error.requestId
                                else -> null
                            }
                            when {
                                requestId != null -> handleResponse(requestId, it)
                                it.hasRunRequest() -> TODO("Not yet implemented")
                                it.hasCancelRequest() -> TODO("Not yet implemented")
                                it.hasNewConfig() -> TODO("Not yet implemented")
                            }
                        }
                    }
                } while (!clientChannel.isClosedForReceive)
            }
        } catch (t: Throwable) {
            logger.error(t) { "Unhandled error in session" }
            throw t
        }
    }

    override suspend fun getCredentialsFor(remote: String): Credentials {
        val msg = credentials.call(remote)
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
        val msg = twoFactor.call(remote)
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
