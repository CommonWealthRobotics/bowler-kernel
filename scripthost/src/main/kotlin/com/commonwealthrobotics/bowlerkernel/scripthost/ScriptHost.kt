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

import com.commonwealthrobotics.bowlerkernel.gitfs.DefaultDependencyResolver
import com.commonwealthrobotics.bowlerkernel.scripting.DefaultScriptLoader
import com.commonwealthrobotics.proto.script_host.ScriptHostGrpc
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import io.grpc.stub.StreamObserver

class ScriptHost : ScriptHostGrpc.ScriptHostImplBase() {

    override fun session(responseObserver: StreamObserver<SessionServerMessage>): StreamObserver<SessionClientMessage> {
        return ScriptHostObserver(DefaultScriptLoader(DefaultDependencyResolver()))
    }
}
