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
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.neuronrobotics.bowlerkernel.hardware.Script
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTimeout
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf
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
        assertEquals("Hello, World!".right(), result)
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
        assertEquals("Hello, World!".right(), result)
    }

    @Test
    fun `a kotlin script that returns a KClass of Script should be instantiated and run`() {
        val scriptContent =
            """
            import arrow.core.Either
            import arrow.core.right
            import com.google.common.collect.ImmutableList
            import com.neuronrobotics.bowlerkernel.hardware.Script

            class Foo : Script() {
                override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                    return args.first().right()
                }

                override fun stopScript() {
                }
            }

            Foo::class
            """.trimIndent()

        val script = DefaultScript(ScriptLanguage.Kotlin, scriptContent)

        val result = script.startScript(immutableListOf(1))
        assertEquals(1.right(), result)
    }

    @Test
    fun `a kotlin script that returns a Class of Script should be instantiated and run`() {
        val scriptContent =
            """
            import arrow.core.Either
            import arrow.core.right
            import com.google.common.collect.ImmutableList
            import com.neuronrobotics.bowlerkernel.hardware.Script

            class Foo : Script() {
                override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                    return args.first().right()
                }

                override fun stopScript() {
                }
            }

            Foo::class.java
            """.trimIndent()

        val script = DefaultScript(ScriptLanguage.Kotlin, scriptContent)

        val result = script.startScript(immutableListOf(1))
        assertEquals(1.right(), result)
    }

    @Test
    fun `a kotlin script that returns a KClass which is not a Script should just be returned`() {
        val scriptContent =
            """
            class Foo
            Foo::class
            """.trimIndent()

        val script = DefaultScript(ScriptLanguage.Kotlin, scriptContent)

        val result = script.startScript(emptyImmutableList())
        assertTrue(result is Either.Right)
        result as Either.Right

        assertThat(result.b!!::class.simpleName!!, containsSubstring("Foo"))
    }

    @Test
    fun `a kotlin script that returns a Class which is not a Script should just be returned`() {
        val scriptContent =
            """
            class Foo
            Foo::class.java
            """.trimIndent()

        val script = DefaultScript(ScriptLanguage.Kotlin, scriptContent)

        val result = script.startScript(emptyImmutableList())
        assertTrue(result is Either.Right)
        result as Either.Right

        assertThat(result.b!!::class.simpleName!!, containsSubstring("Foo"))
    }

    @Test
    fun `a groovy script that returns a Class of Script should be instantiated and run`() {
        val scriptContent =
            """
            import arrow.core.Either
            import com.google.common.collect.ImmutableList
            import com.neuronrobotics.bowlerkernel.hardware.Script
            import org.jetbrains.annotations.NotNull

            class Foo extends Script {

                @Override
                protected Either<String, Object> runScript(@NotNull ImmutableList<Object> args) {
                    return new Either.Right(args.first())
                }

                @Override
                protected void stopScript() {
                }
            }

            Foo
            """.trimIndent()

        val script = DefaultScript(ScriptLanguage.Groovy, scriptContent)

        val result = script.startScript(immutableListOf(1))
        assertEquals(1.right(), result)
    }

    @Test
    fun `a groovy script that returns a Class which is not a Script should just be returned`() {
        val scriptContent =
            """
            class Foo {
            }
            Foo
            """.trimIndent()

        val script = DefaultScript(ScriptLanguage.Groovy, scriptContent)

        val result = script.startScript(emptyImmutableList())
        assertTrue(result is Either.Right)
        result as Either.Right

        assertThat(result.b!!::class.simpleName!!, containsSubstring("Foo"))
    }
}
