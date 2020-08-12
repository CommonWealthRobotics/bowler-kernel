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

import com.commonwealthrobotics.bowlerkernel.scripting.ScriptLoader
import com.commonwealthrobotics.bowlerkernel.testutil.fileSpec
import com.commonwealthrobotics.bowlerkernel.testutil.projectSpec
import com.commonwealthrobotics.bowlerkernel.testutil.runRequest
import com.commonwealthrobotics.bowlerkernel.testutil.sessionClientMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test

internal class ScriptHostObserverTest {

    @Test
    fun `a run request must trigger the script loader`() {
        val scriptLoader = mockk<ScriptLoader> {
            every { resolveAndLoad(any(), any(), any()) } returns mockk()
        }

        val scriptHost = ScriptHostObserver(scriptLoader)
        val file = fileSpec(projectSpec("git@github.com:user/repo1.git", "master", byteArrayOf()), "file1.groovy")
        val devs = listOf(projectSpec("git@github.com:user/repo2.git", "master", byteArrayOf()))
        val environment = mapOf("KEY" to "VALUE")
        scriptHost.onNext(sessionClientMessage(runRequest = runRequest(1, file, devs, environment)))

        verifyOrder {
            scriptLoader.resolveAndLoad(file, devs, environment)
        }
    }
}
