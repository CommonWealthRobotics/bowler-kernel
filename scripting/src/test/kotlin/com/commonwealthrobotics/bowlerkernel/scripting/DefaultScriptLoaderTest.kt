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
package com.commonwealthrobotics.bowlerkernel.scripting

import com.commonwealthrobotics.bowlerkernel.gitfs.DependencyResolver
import com.commonwealthrobotics.bowlerkernel.protoutil.fileSpec
import com.commonwealthrobotics.bowlerkernel.protoutil.patch
import com.commonwealthrobotics.bowlerkernel.protoutil.projectSpec
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test

internal class DefaultScriptLoaderTest {

    private val fileSpec1 = fileSpec(
        projectSpec("git@github.com:user/repo1.git", "master", patch(byteArrayOf())),
        "file1.groovy"
    )

    @Test
    fun `resolveAndLoad a script with no devs and no env that returns a simple value`() {
        val file1 = createTempFile().apply { writeText("1") }
        val dependencyResolver = mockk<DependencyResolver>(relaxUnitFun = true) {
            every { resolve(fileSpec1) } returns file1
        }

        val scriptLoader = DefaultScriptLoader(dependencyResolver)
        val script = scriptLoader.resolveAndLoad(fileSpec1, listOf(), mapOf())
        runScript(script, listOf()).shouldBeRight(1)

        verifyOrder {
            dependencyResolver.resolve(fileSpec1)
        }
    }

    @Test
    fun `resolveAndLoad a script with a compiler error`() {
        val file1 = createTempFile().apply { writeText(" \" ") }
        val dependencyResolver = mockk<DependencyResolver>(relaxUnitFun = true) {
            every { resolve(fileSpec1) } returns file1
        }

        val scriptLoader = DefaultScriptLoader(dependencyResolver)
        val script = scriptLoader.resolveAndLoad(fileSpec1, listOf(), mapOf())
        runScript(script, listOf()).shouldBeLeft()

        verifyOrder {
            dependencyResolver.resolve(fileSpec1)
        }
    }

    @Test
    fun `check the compiled script has the required globals`() {
        val file1 = createTempFile().apply {
            writeText(
                """
                |assert args == [1]
                |assert scriptEnvironment == ["key": "value"]
                |assert scriptExecutionEnvironment instanceof ScriptExecutionEnvironment
                """.trimMargin()
            )
        }
        val dependencyResolver = mockk<DependencyResolver>(relaxUnitFun = true) {
            every { resolve(fileSpec1) } returns file1
        }

        val scriptLoader = DefaultScriptLoader(dependencyResolver)
        val script = scriptLoader.resolveAndLoad(fileSpec1, listOf(), mapOf("key" to "value"))
        runScript(script, listOf(1)).shouldBeRight()

        verifyOrder {
            dependencyResolver.resolve(fileSpec1)
        }
    }
}
