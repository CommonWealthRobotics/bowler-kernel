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
package com.neuronrobotics.bowlerkernel.internal.logging

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

        /**
         * Calls [joinToString] with an indent applied to each separated line.
         *
         * @param indent The indent for each line, typically a tab character.
         * @return The string.
         */
        fun <T> Iterable<T>.joinWithIndent(indent: String) =
            joinToString(separator = "\n$indent", prefix = indent)
    }
}
