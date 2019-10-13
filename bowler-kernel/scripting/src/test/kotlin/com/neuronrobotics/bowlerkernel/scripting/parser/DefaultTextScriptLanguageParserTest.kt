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
package com.neuronrobotics.bowlerkernel.scripting.parser

import com.neuronrobotics.bowlerkernel.scripting.ScriptLanguage
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultTextScriptLanguageParserTest {

    private val parser = DefaultScriptLanguageParser()

    @ParameterizedTest
    @MethodSource("languageParsingSource")
    fun `test language parsing`(data: Pair<String, ScriptLanguage>) {
        val expectedLanguage = data.second
        val parsedLanguage = parser.parse(data.first).fold(
            { fail<ScriptLanguage> { it } },
            { it }
        )
        assertEquals(expectedLanguage, parsedLanguage)
    }

    @Test
    fun `test unknown language`() {
        assertTrue(parser.parse("qwerty").isLeft())
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun languageParsingSource() = listOf(
            "groovy" to ScriptLanguage.Groovy,
            "kotlin" to ScriptLanguage.Kotlin,
            "kts" to ScriptLanguage.Kotlin
        )
    }
}
