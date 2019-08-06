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
@file:SuppressWarnings("LongMethod")

package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.Either
import arrow.core.right
import com.google.common.base.Stopwatch
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.Script
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTimeout
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableList
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultScriptTest {

    @Test
    fun `test run script with a hello world script`() {
        val scriptContent = """ return "Hello, World!" """
        val script = DefaultScript(ScriptLanguage.Groovy, scriptContent)

        val result = script.startScript(emptyImmutableList())
        assertAll(
            { assertTrue(result.isRight()) },
            { assertEquals("Hello, World!", result.fold({ null }, { it as? String })) }
        )
    }

    @Test
    fun `test run script with a parse error`() {
        val scriptContent = """ return "Hello, World!" """
        val script = DefaultScript(ScriptLanguage.Groovy, scriptContent)

        val result = script.startScript(emptyImmutableList())
        assertTrue(result.isLeft())
    }

    @Test
    fun `test run script with a compile error`() {
        val scriptContent = """ " """ // Single quote on purpose
        val script = DefaultScript(ScriptLanguage.Groovy, scriptContent)

        val result = script.startScript(emptyImmutableList())
        assertTrue(result.isLeft())
    }

    @Test
    fun `test interrupting a script`() {
        val scriptContent = """
            |try {
            |   Thread.sleep(10000)
            |} catch (InterruptedException ignored) {
            |   return
            |}
            """.trimMargin()

        val script = DefaultScript(ScriptLanguage.Groovy, scriptContent)

        assertTimeout(Duration.ofSeconds(2)) {
            thread { script.startScript(emptyImmutableList()) }
            Thread.sleep(1000)
            script.stopAndCleanUp()
        }
    }

    @Test
    fun `test stopping a script which has child threads`() {
        val script = object : Script() {
            override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                repeat(2) {
                    addThread(thread {
                        Thread.sleep(10000)
                    })
                }

                return Unit.right()
            }

            @SuppressWarnings("EmptyFunctionBlock")
            override fun stopScript() {
            }
        }

        script.startScript(emptyImmutableList())

        Thread.sleep(500)
        assertTrue(script.isRunning)

        val stopwatch = Stopwatch.createStarted()
        script.stopAndCleanUp(timeout = 1000)
        assertEquals(2, stopwatch.stop().elapsed(TimeUnit.SECONDS))
        assertFalse(script.isRunning)
    }

    @Test
    fun `test run script with a hello world script using kotlin`() {
        val scriptContent = """ "Hello, World!" """

        val script = DefaultScript(ScriptLanguage.Kotlin, scriptContent)

        val result = script.startScript(emptyImmutableList())
        assertAll(
            { assertTrue(result.isRight()) },
            { assertEquals("Hello, World!", result.fold({ null }, { it as? String })) }
        )
    }
}
