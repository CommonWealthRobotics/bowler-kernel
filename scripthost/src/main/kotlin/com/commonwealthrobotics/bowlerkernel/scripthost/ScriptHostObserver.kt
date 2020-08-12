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

import com.commonwealthrobotics.bowlerkernel.scripting.Script
import com.commonwealthrobotics.bowlerkernel.scripting.ScriptLoader
import com.commonwealthrobotics.proto.script_host.ConfirmationResponse
import com.commonwealthrobotics.proto.script_host.CredentialsResponse
import com.commonwealthrobotics.proto.script_host.RequestError
import com.commonwealthrobotics.proto.script_host.RunRequest
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.TwoFactorResponse
import io.grpc.stub.StreamObserver

class ScriptHostObserver(
    private val scriptLoader: ScriptLoader
) : StreamObserver<SessionClientMessage> {

    private val scriptRequestMap = mutableMapOf<Long, Script>()

    override fun onNext(value: SessionClientMessage) {
        when {
            value.hasRunRequest() -> onRunRequest(value.runRequest)
            value.hasConfirmationResponse() -> onConfirmationResponse(value.confirmationResponse)
            value.hasCredentialsResponse() -> onCredentialsResponse(value.credentialsResponse)
            value.hasTwoFactorResponse() -> on2FAResponse(value.twoFactorResponse)
            value.hasError() -> onRequestError(value.error)
            else -> error("Unrecognized value: $value")
        }
    }

    override fun onError(t: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun onCompleted() {
        TODO("Not yet implemented")
    }

    private fun onRunRequest(runRequest: RunRequest) {
        scriptRequestMap[runRequest.requestId] =
            scriptLoader.resolveAndLoad(runRequest.file, runRequest.devsList, runRequest.environmentMap)
    }

    private fun onConfirmationResponse(confirmationResponse: ConfirmationResponse) {
        TODO("Not yet implemented")
    }

    private fun onCredentialsResponse(credentialsResponse: CredentialsResponse) {
        TODO("Not yet implemented")
    }

    private fun on2FAResponse(twoFactorResponse: TwoFactorResponse) {
        TODO("Not yet implemented")
    }

    private fun onRequestError(error: RequestError) {
        TODO("Not yet implemented")
    }
}
