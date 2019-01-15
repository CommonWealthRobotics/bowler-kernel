/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.logging

import com.neuronrobotics.bowlerkernel.settings.BOWLERKERNEL_DIRECTORY
import com.neuronrobotics.bowlerkernel.settings.BOWLER_DIRECTORY
import com.neuronrobotics.bowlerkernel.settings.LOGS_DIRECTORY
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.logging.ConsoleHandler
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

class LoggerUtilities private constructor() {

    init {
        throw UnsupportedOperationException("This is a utility class!")
    }

    // We can'translate call a logger here instead because we are the logger!
    @SuppressWarnings("PrintStackTrace")
    companion object {

        // Log file parent directory path
        private val logFileDirPath: String = Paths.get(
            System.getProperty("user.home"),
            BOWLER_DIRECTORY,
            BOWLERKERNEL_DIRECTORY,
            LOGS_DIRECTORY
        ).toAbsolutePath().toString()

        // Log file path
        private val logFilePath: String = Paths.get(
            logFileDirPath,
            SimpleDateFormat("yyyyMMddHHmmss'.txt'", Locale("en", "US"))
                .format(Date())
        ).toAbsolutePath().toString()

        // FileHandler that saves to the log file
        private var fileHandler: FileHandler? = null

        // Previous logger names
        private val loggerNames = mutableSetOf<String>()

        init {
            val testFile = File(logFileDirPath)
            try {
                if (testFile.exists() || testFile.mkdirs()) {
                    fileHandler = FileHandler(
                        logFilePath, true
                    )
                    fileHandler!!.formatter = SimpleFormatter()
                } else {
                    throw IOException(
                        "LoggerUtilities could not create the logging file: $logFilePath"
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        /**
         * Setup a logger with handlers and set its log level to [Level.ALL].
         *
         * @param name The logger name.
         * @return The new logger.
         */
        fun getLogger(name: String): Logger {
            if (!loggerNames.add(name)) {
                throw UnsupportedOperationException(
                    "Cannot add logger of name: $name. A logger with the same name already exists."
                )
            }

            return Logger.getLogger(name).apply {
                addHandler(ConsoleHandler())
                addHandler(fileHandler!!)
                level = Level.ALL
            }
        }
    }
}
