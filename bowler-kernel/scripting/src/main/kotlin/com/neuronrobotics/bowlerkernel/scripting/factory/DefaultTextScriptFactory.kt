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

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.scripting.DefaultScript
import com.neuronrobotics.bowlerkernel.scripting.ScriptLanguage
import com.neuronrobotics.bowlerkernel.scripting.parser.ScriptLanguageParser
import javax.inject.Inject

class DefaultTextScriptFactory
@Inject internal constructor(
    private val scriptLanguageParser: ScriptLanguageParser
) : TextScriptFactory {

    /**
     * Creates a [DefaultScript] from text.
     *
     * @param language A string representing the script language.
     * @param scriptText The text content of the script.
     * @return A [DefaultScript] on success, a [String] on error.
     */
    override fun createScriptFromText(
        language: String,
        scriptText: String
    ): Either<String, DefaultScript> =
        scriptLanguageParser.parse(language).map {
            DefaultScript(it, scriptText)
        }

    /**
     * Creates a [DefaultScript] from text.
     *
     * @param language A string representing the script language.
     * @param scriptText The text content of the script.
     * @return A [DefaultScript] on success, a [String] on error.
     */
    override fun createScriptFromText(
        language: ScriptLanguage,
        scriptText: String
    ) = DefaultScript(language, scriptText)
}
