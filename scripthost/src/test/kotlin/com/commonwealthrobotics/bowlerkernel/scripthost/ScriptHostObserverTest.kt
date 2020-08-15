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
import com.commonwealthrobotics.bowlerkernel.protoutil.fileSpec
import com.commonwealthrobotics.bowlerkernel.protoutil.newTask
import com.commonwealthrobotics.bowlerkernel.protoutil.patch
import com.commonwealthrobotics.bowlerkernel.protoutil.projectSpec
import com.commonwealthrobotics.bowlerkernel.protoutil.runRequest
import com.commonwealthrobotics.bowlerkernel.protoutil.sessionClientMessage
import com.commonwealthrobotics.bowlerkernel.protoutil.sessionServerMessage
import com.commonwealthrobotics.bowlerkernel.protoutil.taskEnd
import com.commonwealthrobotics.bowlerkernel.protoutil.taskUpdate
import com.commonwealthrobotics.bowlerkernel.scripting.Script
import com.commonwealthrobotics.bowlerkernel.scripting.ScriptLoader
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import io.grpc.stub.StreamObserver
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test

internal class ScriptHostObserverTest {

    @Test
    fun `a run request must trigger the script loader`() {
        val responseObserver = mockk< StreamObserver<SessionServerMessage>>(relaxUnitFun = true) {
        }
        // Have the script return nothing interesting
        val script = mockk<Script>(relaxUnitFun = true) {
            every { join(any(), any(), any()) } returns Either.Right(Unit)
        }
        val scriptLoader = mockk<ScriptLoader> {
            every { resolveAndLoad(any(), any(), any()) } returns script
        }

        val scriptHost = ScriptHostObserver(responseObserver, scriptLoader)
        val file = fileSpec(
            projectSpec("git@github.com:user/repo1.git", "master", patch(byteArrayOf())),
            "file1.groovy"
        )
        val devs = listOf(projectSpec("git@github.com:user/repo2.git", "master", patch(byteArrayOf())))
        val environment = mapOf("KEY" to "VALUE")
        scriptHost.onNext(sessionClientMessage(runRequest = runRequest(1, file, devs, environment)))

        verifyOrder {
            // Initialize the script
            responseObserver.onNext(
                sessionServerMessage(
                    newTask = newTask(1, "Initializing file1.groovy", taskUpdate(0, Float.NaN))
                )
            )
            scriptLoader.resolveAndLoad(file, devs, environment)
            responseObserver.onNext(sessionServerMessage(taskEnd = taskEnd(0)))

            // Run the script
            responseObserver.onNext(
                sessionServerMessage(
                    newTask = newTask(1, "Running file1.groovy", taskUpdate(1, Float.NaN))
                )
            )
            script.start(emptyList(), null)
            script.join(any(), any(), any())
            responseObserver.onNext(sessionServerMessage(taskEnd = taskEnd(1)))
        }
    }
}
