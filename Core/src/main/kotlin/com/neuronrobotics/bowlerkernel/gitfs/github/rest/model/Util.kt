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
