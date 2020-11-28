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
package com.commonwealthrobotics.bowlerkernel.util

import mu.KotlinLogging
import java.io.File

fun run(dir: File, vararg cmd: String): Process = ProcessBuilder(*cmd).directory(dir).start().also {
    val exitCode = it.waitFor()
    check(exitCode == 0) {
        """
            |Process exited with a non-zero exit code.
            |exit code: $exitCode
            |dir: $dir
            |cmd: ${cmd.joinToString()}
            """.trimMargin()
    }
}

fun runAndPrintOutput(dir: File, vararg cmd: String): Process = ProcessBuilder(*cmd).directory(dir).start().also {
    val exitCode = it.waitFor()
    ProcessUtil.logger.debug { it.inputStream.readAllBytes().decodeToString() }
    ProcessUtil.logger.debug { it.errorStream.readAllBytes().decodeToString() }
    check(exitCode == 0) {
        """
            |Process exited with a non-zero exit code.
            |exit code: $exitCode
            |dir: $dir
            |cmd: ${cmd.joinToString()}
            """.trimMargin()
    }
}

object ProcessUtil {
    internal val logger = KotlinLogging.logger { }
}
