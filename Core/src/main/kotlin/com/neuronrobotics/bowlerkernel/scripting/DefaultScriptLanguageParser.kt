/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.left
import arrow.core.right

class DefaultScriptLanguageParser : ScriptLanguageParser {

    override fun parse(language: String) =
        when (language.toLowerCase()) {
            "groovy" -> ScriptLanguage.Groovy.right()
            else ->
                """
                |Unknown language:
                |$language
                """.trimMargin().left()
        }
}
