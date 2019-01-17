/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.scripting.parser

import com.neuronrobotics.bowlerkernel.scripting.ScriptLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class DefaultScriptLanguageParserTest {

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
