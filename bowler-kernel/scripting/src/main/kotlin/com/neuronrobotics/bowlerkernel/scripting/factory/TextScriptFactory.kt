/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.scripting.factory

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.hardware.Script

interface TextScriptFactory {

    /**
     * Creates a [Script] from text.
     *
     * @param language A string representing the script language.
     * @param scriptText The text content of the script.
     * @return A [Script] on success, a [String] on error.
     */
    fun createScriptFromText(language: String, scriptText: String): Either<String, Script>
}
