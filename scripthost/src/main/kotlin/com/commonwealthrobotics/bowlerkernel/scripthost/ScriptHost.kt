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
import com.commonwealthrobotics.proto.script_host.CredentialsRequest
import com.commonwealthrobotics.proto.script_host.CredentialsResponse
import com.commonwealthrobotics.proto.script_host.RequestError
import com.commonwealthrobotics.proto.script_host.ScriptHostGrpc
import com.commonwealthrobotics.proto.script_host.ScriptHostGrpcKt
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transform
import mu.KotlinLogging

class ScriptHost : ScriptHostGrpc.ScriptHostImplBase() {

    override fun session(responseObserver: StreamObserver<SessionServerMessage>): StreamObserver<SessionClientMessage> {
        return ScriptHostObserver(responseObserver)
    }
}

class Foo : ScriptHostGrpcKt.ScriptHostCoroutineImplBase() {
    override fun session(requests: Flow<SessionClientMessage>): Flow<SessionServerMessage> = Bar(requests).session
}

class Bar(
        private val client: Flow<SessionClientMessage>
) : CredentialsProvider {

    val session: Flow<SessionServerMessage> = client.map { msg ->
        when {
            msg.hasRunRequest() -> TODO("")
            msg.hasNewConfig() -> TODO("")
            msg.hasCancelRequest() -> TODO("")
            else -> TODO("")
        }
    }

    override suspend fun getCredentialsFor(remote: String): Credentials {
        TODO("Not yet implemented")
    }

    override fun getTwoFactorFor(remote: String): String {
        TODO("Not yet implemented")
    }

    companion object {
        private val logger = KotlinLogging.logger {  }
    }
}
