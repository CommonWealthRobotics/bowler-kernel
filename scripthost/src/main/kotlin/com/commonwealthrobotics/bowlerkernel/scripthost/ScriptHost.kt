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
import com.commonwealthrobotics.proto.script_host.ScriptHostGrpcKt
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicLong

@OptIn(ExperimentalCoroutinesApi::class)
class ScriptHost : ScriptHostGrpcKt.ScriptHostCoroutineImplBase() {

    override fun session(requests: Flow<SessionClientMessage>): Flow<SessionServerMessage> =
        ScriptSession(requests.toChannel()).sessionFlow
}

@OptIn(ExperimentalCoroutinesApi::class)
class ScriptSession(
    _client: ReceiveChannel<SessionClientMessage>
) : CredentialsProvider {

    private val client = _client.receiveAsFlow().map { logger.debug { "Received $it" }; it }.toChannel()
    private val session = Channel<SessionServerMessage>()
    private val nextRequestId = AtomicLong(1)

    val sessionFlow = session.receiveAsFlow().map { logger.debug { "Sending $it" }; it }

    override suspend fun getCredentialsFor(remote: String): Credentials {
        session.send(
            SessionServerMessage.newBuilder().apply {
                credentialsRequestBuilder.apply {
                    setRemote(remote)
                    // TODO: Either remove task id from the credentials request or setup some task context thing
                    taskId = 1
                    requestId = nextRequestId.getAndIncrement()
                }
            }.build()
        )

        val msg = client.receive()

        return when {
            msg.hasCredentialsResponse() -> {
                val res = msg.credentialsResponse
                when {
                    res.hasBasic() -> Credentials.Basic(res.basic.username, res.basic.password)
                    res.hasOauth() -> Credentials.OAuth(res.oauth.token)
                    else -> Credentials.Anonymous
                }
            }
            else -> error(msg)
        }
    }

    override suspend fun getTwoFactorFor(remote: String): String {
        session.send(
            SessionServerMessage.newBuilder().apply {
                twoFactorRequestBuilder.apply {
                    description = remote
                    requestId = nextRequestId.getAndIncrement()
                }
            }.build()
        )

        val msg = client.receive()

        return when {
            msg.hasTwoFactorResponse() -> msg.twoFactorResponse.twoFactor
            else -> error(msg)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
