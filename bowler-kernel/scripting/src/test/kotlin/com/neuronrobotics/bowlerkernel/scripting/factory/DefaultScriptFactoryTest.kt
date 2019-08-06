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
package com.neuronrobotics.bowlerkernel.scripting.factory

import arrow.core.left
import arrow.core.right
import com.neuronrobotics.bowlerkernel.scripting.ScriptLanguage
import com.neuronrobotics.bowlerkernel.scripting.parser.ScriptLanguageParser
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DefaultScriptFactoryTest {

    @Test
    fun `creating a script with a known language succeeds`() {
        val language = "groovy"
        val mockScriptLanguageParser = mockk<ScriptLanguageParser> {
            every { parse(language) } returns ScriptLanguage.Groovy.right()
        }

        val script = DefaultScriptFactory(
            mockScriptLanguageParser
        ).createScriptFromText(language, "")

        assertTrue(script.isRight())
    }

    @Test
    fun `creating a script with an unknown language fails`() {
        val unknownLanguage = "unknownLanguage"
        val mockScriptLanguageParser = mockk<ScriptLanguageParser> {
            every { parse(unknownLanguage) } returns "".left()
        }

        val script = DefaultScriptFactory(
            mockScriptLanguageParser
        ).createScriptFromText(unknownLanguage, "")

        assertTrue(script.isLeft())
    }
}
