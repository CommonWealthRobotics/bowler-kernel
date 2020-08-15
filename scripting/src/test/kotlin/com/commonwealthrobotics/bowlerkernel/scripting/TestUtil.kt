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
package com.commonwealthrobotics.bowlerkernel.scripting

import arrow.core.Either
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import java.util.concurrent.TimeUnit

/**
 * Runs the [script] with the [args] and asserts some basic things about its contract. Returns the script's result.
 */
internal fun runScript(
    script: Script,
    args: List<Any?>,
    parent: Script? = null
): Either<Throwable, Any?> {
    script.isRunning.shouldBeFalse()

    script.start(args, parent)
    script.isRunning.shouldBeTrue()

    val value = script.join()
    script.isRunning.shouldBeFalse()

    return value
}

/**
 * Runs the [script] with the [args] and asserts some basic things about its contract. Returns the script's result.
 */
@SuppressWarnings("LongParameterList") // This is fine because it's a test utility method
internal fun runScript(
    script: Script,
    args: List<Any?>,
    parent: Script? = null,
    scriptTimeout: Long = 0,
    threadTimeout: Long = 1000,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS
): Either<Throwable, Any?> {
    script.isRunning.shouldBeFalse()

    script.start(args, parent)
    script.isRunning.shouldBeTrue()

    val value = script.join(scriptTimeout, threadTimeout, timeUnit)
    script.isRunning.shouldBeFalse()

    return value
}
