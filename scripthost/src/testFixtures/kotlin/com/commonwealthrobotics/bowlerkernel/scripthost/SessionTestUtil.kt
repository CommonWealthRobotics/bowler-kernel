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

import com.commonwealthrobotics.bowlerkernel.util.toChannel
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent

/**
 * Runs a [Session] in a structured manner.
 *
 * @param client Implements the client. Use this to send requests to and receive responses from the server. The receiver
 * is the client's channel and the first parameter is the server's channel.
 * @return The [Session].
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun runSession(
    koinComponent: KoinComponent,
    client: suspend SendChannel<SessionClientMessage>.(ReceiveChannel<SessionServerMessage>) -> Unit
): Session {
    val channel = Channel<Flow<SessionServerMessage>>()
    val scope = CoroutineScope(Dispatchers.Default)

    val clientFlow = scope.produce {
        val serverFlow = channel.receive()
        client(serverFlow.toChannel(scope))
    }.consumeAsFlow()

    val session = Session(scope, clientFlow, koinComponent)
    runBlocking { channel.send(session.server) }
    return session
}
