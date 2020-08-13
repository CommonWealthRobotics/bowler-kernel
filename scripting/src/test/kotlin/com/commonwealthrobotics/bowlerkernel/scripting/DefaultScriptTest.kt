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

import com.commonwealthrobotics.bowlerkernel.proto.fileSpec
import com.commonwealthrobotics.bowlerkernel.proto.patch
import com.commonwealthrobotics.bowlerkernel.proto.projectSpec
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultScriptTest {

    @Test
    fun `return a simple value`() {
        val script = DefaultScript({ _, _ -> 1 }, mockk())
        runScript(script, listOf()).shouldBeRight(1)
    }

    @Test
    fun `throw an inside exception the script`() {
        val script = DefaultScript({ _, _ -> error("Boom!") }, mockk())
        runScript(script, listOf()).shouldBeLeft {
            it.shouldBeInstanceOf<IllegalStateException> {
                it.message.shouldBe("Boom!")
            }
        }
    }

    @Test
    fun `join and interrupt a script that has child threads`() {
        val threadLatch = CountDownLatch(2)

        val script = DefaultScript(
            { _, exec ->
                repeat(2) {
                    exec.addThread(
                        thread {
                            try {
                                Thread.sleep(100000)
                            } catch (ex: InterruptedException) {
                                threadLatch.countDown()
                            }
                        }
                    )
                }
            },
            mockk()
        )

        runScript(script, listOf()).shouldBeRight()
        threadLatch.count.shouldBe(0)
    }

    @Test
    fun `timeout a script`() {
        val script = DefaultScript({ _, _ -> Thread.sleep(100000) }, mockk())
        runScript(script, listOf(), scriptTimeout = 1000L).shouldBeLeft()
    }

    @Test
    fun `cannot start a script that is its own parent`() {
        val script = DefaultScript({ _, _ -> Unit }, mockk())
        shouldThrow<IllegalArgumentException> {
            runScript(script, listOf(), parent = script, scriptTimeout = 1000L)
        }
    }

    @Test
    fun `cannot start a script that is already running`() {
        val script = DefaultScript({ _, _ -> Thread.sleep(100000) }, mockk())

        script.start(listOf(), null)
        script.isRunning.shouldBeTrue()

        shouldThrow<UnsupportedOperationException> {
            script.start(listOf(), null)
        }
    }

    @Test
    fun `resolve and load a script inside a script`() {
        val fileSpec1 = fileSpec(
            projectSpec("git@github.com:user/repo1.git", "master", patch(byteArrayOf())),
            "file1.groovy"
        )

        val scriptLoader = mockk<ScriptLoader> {
            every { resolveAndLoad(any(), any(), any()) } returns mockk()
        }

        val scriptEnvironment = mapOf("key" to "value")
        val script = DefaultScript({ _, exec -> exec.resolveAndLoad(fileSpec1, scriptEnvironment) }, scriptLoader)
        runScript(script, listOf()).shouldBeRight()

        verifyOrder {
            scriptLoader.resolveAndLoad(fileSpec1, listOf(), scriptEnvironment)
        }
    }
}
