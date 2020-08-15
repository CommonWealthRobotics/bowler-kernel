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
import arrow.core.extensions.fx
import arrow.core.nonFatalOrThrow
import com.commonwealthrobotics.bowlerkernel.authservice.Credentials
import com.commonwealthrobotics.bowlerkernel.authservice.CredentialsProvider
import com.commonwealthrobotics.bowlerkernel.di.BowlerKernelKoinComponent
import com.commonwealthrobotics.bowlerkernel.gitfs.GitFS
import com.commonwealthrobotics.bowlerkernel.gitfs.GitHubFS
import com.commonwealthrobotics.bowlerkernel.protoutil.withTask
import com.commonwealthrobotics.bowlerkernel.scripting.Script
import com.commonwealthrobotics.bowlerkernel.scripting.ScriptLoader
import com.commonwealthrobotics.proto.script_host.ConfirmationResponse
import com.commonwealthrobotics.proto.script_host.CredentialsResponse
import com.commonwealthrobotics.proto.script_host.RequestError
import com.commonwealthrobotics.proto.script_host.RunRequest
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import com.commonwealthrobotics.proto.script_host.TwoFactorResponse
import io.grpc.stub.StreamObserver
import mu.KotlinLogging
import org.koin.core.get
import org.koin.dsl.module

class ScriptHostObserver(
    private val responseObserver: StreamObserver<SessionServerMessage>
) : StreamObserver<SessionClientMessage>, CredentialsProvider, BowlerKernelKoinComponent {

    private val scriptRequestMap = mutableMapOf<Long, Script>()

    override fun onNext(value: SessionClientMessage) {
        @Suppress("ThrowableNotThrown")
        val status = when {
            value.hasRunRequest() -> onRunRequest(value.runRequest)
            value.hasConfirmationResponse() -> onConfirmationResponse(value.confirmationResponse)
            value.hasCredentialsResponse() -> onCredentialsResponse(value.credentialsResponse)
            value.hasTwoFactorResponse() -> on2FAResponse(value.twoFactorResponse)
            value.hasError() -> onRequestError(value.error)
            else -> Either.Left(IllegalStateException("Unrecognized value: $value"))
        }

        // Just log the error because we will have already sent a RequestError if necessary
        status.mapLeft {
            logger.error(it) { "Error responding to $value" }
        }
    }

    override fun onError(t: Throwable) {
        val ex = t.nonFatalOrThrow()
        logger.error(ex) { "" }
    }

    override fun onCompleted() {
        responseObserver.onCompleted()
    }

    override fun getCredentialsFor(remote: String): Credentials {
        TODO("Not yet implemented")
    }

    override fun getTwoFactorFor(remote: String): String {
        TODO("Not yet implemented")
    }

    private fun onRunRequest(runRequest: RunRequest): Either<Throwable, Unit> = Either.fx {
        val modules = listOf(
            module {
                single<GitFS> { GitHubFS(this@ScriptHostObserver) }
            }
        )
        getKoin().loadModules(modules)

        val scriptLoader = get<ScriptLoader>()

        val script = responseObserver.withTask(runRequest.requestId, "Initializing ${runRequest.file.path}") {
            val script = scriptLoader.resolveAndLoad(runRequest.file, runRequest.devsList, runRequest.environmentMap)
            scriptRequestMap[runRequest.requestId] = script
            script
        }.bind()

        responseObserver.withTask(runRequest.requestId, "Running ${runRequest.file.path}") {
            script.start(emptyList(), null)
            val scriptResult = script.join()
            logger.info { "Script returned:\n$scriptResult" }
        }.bind()

        getKoin().unloadModules(modules)
    }

    private fun onConfirmationResponse(confirmationResponse: ConfirmationResponse): Either<Throwable, Unit> {
        TODO("Not yet implemented")
    }

    private fun onCredentialsResponse(credentialsResponse: CredentialsResponse): Either<Throwable, Unit> {
        TODO("Not yet implemented")
    }

    private fun on2FAResponse(twoFactorResponse: TwoFactorResponse): Either<Throwable, Unit> {
        TODO("Not yet implemented")
    }

    private fun onRequestError(error: RequestError): Either<Throwable, Unit> {
        TODO("Not yet implemented")
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
