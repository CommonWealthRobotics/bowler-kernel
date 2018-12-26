/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.Either

interface ScriptFactory {

    /**
     * Creates a [DefaultScript] from a gist.
     *
     * @param gistId The gist id.
     * @param filename The file name in the gist.
     * @return A [DefaultScript] on success, a [String] on error.
     */
    fun createScriptFromGist(gistId: String, filename: String): Either<String, DefaultScript>

    /**
     * Creates a [DefaultScript] from text.
     *
     * @param language A string representing the script language.
     * @param scriptText The text content of the script.
     * @return A [DefaultScript] on success, a [String] on error.
     */
    fun createScriptFromText(language: String, scriptText: String): Either<String, DefaultScript>
}
