/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.scripting.factory

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.scripting.Script

interface GistScriptFactory {

    /**
     * Creates a [Script] from a gist.
     *
     * @param gistId The gist id.
     * @param filename The file name in the gist.
     * @return A [Script] on success, a [String] on error.
     */
    fun createScriptFromGist(gistId: String, filename: String): Either<String, Script>
}
