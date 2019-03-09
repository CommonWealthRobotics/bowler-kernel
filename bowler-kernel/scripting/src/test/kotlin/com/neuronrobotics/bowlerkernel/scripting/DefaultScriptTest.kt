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
package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.google.inject.Binding
import com.google.inject.Key
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.neuronrobotics.bowlerkernel.scripting.factory.DefaultTextScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.parser.DefaultScriptLanguageParser
import com.neuronrobotics.bowlerkernel.scripting.parser.ScriptLanguageParser
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.jlleitschuh.guice.module
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTimeout
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.emptyImmutableList
import java.time.Duration
import kotlin.concurrent.thread

internal class DefaultScriptTest {

    @Test
    fun `test run script with a hello world script`() {
        val language = "groovy"
        val scriptContent = """ return "Hello, World!" """

        val mockScriptLanguageParser = mock<ScriptLanguageParser> {
            on { parse(language) } doReturn ScriptLanguage.Groovy.right()
        }

        val script = DefaultTextScriptFactory(
            mockScriptLanguageParser
        ).createScriptFromText(language, scriptContent)

        val result = script.flatMap { it.runScript(emptyImmutableList()) }
        assertAll(
            { assertTrue(result.isRight()) },
            { assertEquals("Hello, World!", result.fold({ null }, { it as? String })) }
        )
    }

    @Test
    fun `test run script with a parse error`() {
        val language = "groovy"
        val scriptContent = """ return "Hello, World!" """

        val mockScriptLanguageParser = mock<ScriptLanguageParser> {
            on { parse(language) } doReturn "".left()
        }

        val script = DefaultTextScriptFactory(
            mockScriptLanguageParser
        ).createScriptFromText(language, scriptContent)

        val result = script.flatMap { it.runScript(emptyImmutableList()) }
        assertTrue(result.isLeft())
    }

    @Test
    fun `test run script with a compile error`() {
        val language = "groovy"
        val scriptContent = """ " """ // Single quote on purpose

        val mockScriptLanguageParser = mock<ScriptLanguageParser> {
            on { parse(language) } doReturn ScriptLanguage.Groovy.right()
        }

        val script = DefaultTextScriptFactory(
            mockScriptLanguageParser
        ).createScriptFromText(language, scriptContent)

        val result = script.flatMap { it.runScript(emptyImmutableList()) }
        assertTrue(result.isLeft())
    }

    @Test
    fun `test interrupting a script`() {
        val language = "groovy"
        val scriptContent = """
            |try {
            |   Thread.sleep(10000)
            |} catch (InterruptedException ignored) {
            |   return
            |}
            """.trimMargin()

        val mockScriptLanguageParser = mock<ScriptLanguageParser> {
            on { parse(language) } doReturn ScriptLanguage.Groovy.right()
        }

        val script = DefaultTextScriptFactory(
            mockScriptLanguageParser
        ).createScriptFromText(language, scriptContent)

        script.bimap(
            {
                fail<Unit> {
                    """
                    |Script was a left:
                    |$script
                    """.trimMargin()
                }
            },
            {
                assertTimeout(Duration.ofSeconds(2)) {
                    thread { it.runScript(emptyImmutableList()) }
                    Thread.sleep(1000)
                    it.stopAndCleanUp()
                }
            }
        )
    }

    @Test
    fun `test run script with a hello world script using kotlin`() {
        val language = "kotlin"
        val scriptContent = """ "Hello, World!" """

        val mockScriptLanguageParser = mock<ScriptLanguageParser> {
            on { parse(language) } doReturn ScriptLanguage.Kotlin.right()
        }

        val script = DefaultTextScriptFactory(
            mockScriptLanguageParser
        ).createScriptFromText(language, scriptContent)

        val result = script.flatMap { it.runScript(emptyImmutableList()) }
        assertAll(
            { assertTrue(result.isRight()) },
            { assertEquals("Hello, World!", result.fold({ null }, { it as? String })) }
        )
    }

    @Test
    fun `test run script with unknown language`() {
        val mockScriptLanguageParser = mock<ScriptLanguageParser> {
            on { parse("qwerty") } doReturn "".left()
        }

        val script = DefaultTextScriptFactory(
            mockScriptLanguageParser
        ).createScriptFromText("qwerty", "")

        assertTrue(script.isLeft())
    }

    @Test
    fun `test injector adds existing modules`() {
        val scriptText = """
        import arrow.core.Either
        import arrow.core.right
        import com.google.common.collect.ImmutableList
        import com.neuronrobotics.bowlerkernel.hardware.Script

        class TestScript : Script() {
            override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                return injector.allBindings.right()
            }

            override fun stopScript() {
            }
        }

        TestScript::class
        """.trimIndent()

        val script = DefaultTextScriptFactory(
            DefaultScriptLanguageParser()
        ).createScriptFromText("kotlin", scriptText).fold(
            {
                fail<DefaultScript> {
                    """
                    |Failed to create script:
                    |$it
                    """.trimMargin()
                }
            },
            { it }
        )

        script.addToInjector(module {
            bind<IFoo>().to<Foo>()
        })

        // Run the script a first time, this should work fine
        script.runScript(emptyImmutableList()).bimap(
            {
                fail<Boolean> {
                    """
                    |Failed to run script:
                    |$it
                    """.trimMargin()
                }
            },
            {
                @Suppress("UNCHECKED_CAST")
                val bindings = it as Map<Key<*>, Binding<*>>

                assertThat(
                    bindings.keys.filter { it.typeLiteral.rawType == IFoo::class.java },
                    hasSize(equalTo(1))
                )
            }
        )
    }

    private interface IFoo
    private class Foo : IFoo
}
