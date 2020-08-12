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
import java.util.concurrent.TimeUnit

interface Script {

    /**
     * Whether the script is currently running.
     */
    val isRunning: Boolean

    /**
     * Start running this script with the `args`. This method is quick to execute and does not block while the script
     * runs. A `parent` script MUST be specified if this script is being started by another script. A script may be run
     * multiple times, but may not be run when it is already running.
     *
     * @param args The arguments to pass to this script.
     * @param parent The parent of this script, or `null` if this is a top-level script.
     */
    fun start(args: List<Any?>, parent: Script?)

    /**
     * Wait for this script to finish and return its result as an [Either.Right]. If the script threw a non-fatal
     * exception, that exception is returned as an [Either.Left] instead.
     *
     * A script is finished when its thread and all of its child threads have been joined. After a script has been
     * joined, it may be run again.
     *
     * The script will be timed out and interrupted after the [scriptTimeout]. The script's child threads will be timed
     * out and interrupted after the [threadTimeout].
     *
     * @param scriptTimeout The maximum amount of time the script can run for. A value of zero means to wait forever.
     * @param threadTimeout The maximum amount of time the script's child threads can each run for. A value of zero
     * means to wait forever.
     * @param timeUnit The unit of the timeouts.
     * @return The result of the script or an error.
     */
    fun join(
        scriptTimeout: Long = 0,
        threadTimeout: Long = 1000,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS
    ): Either<Throwable, Any?>
}
