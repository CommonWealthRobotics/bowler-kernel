/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.gitfs.github.rest.model

/**
 * Validate a gist's file names.
 */
internal fun validateFilenames(files: Map<String, Any?>) =
    files.forEach { filename, _ ->
        require(!filename.startsWith("gistfile")) {
            "Gist filename may not start with a \"gistfile\" prefix."
        }
    }
