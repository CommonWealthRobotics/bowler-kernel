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

import com.commonwealthrobotics.proto.script_host.ScriptHostGrpcKt
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import java.util.concurrent.atomic.AtomicInteger

class ScriptHost(private val scope: CoroutineScope) : ScriptHostGrpcKt.ScriptHostCoroutineImplBase() {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun session(requests: Flow<SessionClientMessage>): Flow<SessionServerMessage> {
        if (concurrentSessionCount.getAndIncrement() != 0) {
            concurrentSessionCount.decrementAndGet()
            error("Cannot create a concurrent session. Only one session may operate at a time.")
        }

        return Session(scope, requests).server.onCompletion { concurrentSessionCount.decrementAndGet() }
    }

    companion object {
        private val concurrentSessionCount = AtomicInteger(0)
    }
}
