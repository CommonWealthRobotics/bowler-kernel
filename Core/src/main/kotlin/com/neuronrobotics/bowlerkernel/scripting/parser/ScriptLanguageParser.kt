/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.scripting.parser

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.scripting.ScriptLanguage

/**
 * Parses a script language from its language string.
 */
interface ScriptLanguageParser {

    /**
     * Parses a script language from the language string.
     *
     * @param language The language string.
     * @return The script language.
     */
    fun parse(language: String): Either<ScriptLanguageParseError, ScriptLanguage>
}
