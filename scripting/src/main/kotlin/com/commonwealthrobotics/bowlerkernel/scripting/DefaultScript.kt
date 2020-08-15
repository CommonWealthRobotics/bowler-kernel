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
import arrow.core.left
import arrow.core.nonFatalOrThrow
import arrow.core.right
import arrow.syntax.function.partially1
import com.commonwealthrobotics.proto.gitfs.FileSpec
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @param scriptClosure The closure that will be given the script's args to run the script.
 * @param scriptLoader The [ScriptLoader] that the script will use to load other scripts when it runs.
 */
class DefaultScript(
    private val scriptClosure: ScriptClosure,
    private val scriptLoader: ScriptLoader
) : Script, ScriptExecutionEnvironment {

    /**
     * Whether the script is currently running.
     */
    override val isRunning
        get() = internalIsRunning.get()

    private var internalIsRunning = AtomicBoolean(false)
    private lateinit var returnValue: Future<Any?>
    private val executor = Executors.newSingleThreadExecutor()

    /**
     * The child threads the script created.
     */
    private val threads = mutableListOf<Thread>()

    override fun start(args: List<Any?>, parent: Script?) {
        require(parent != this) {
            "A script cannot be its own parent."
        }

        if (isRunning) {
            throw UnsupportedOperationException("Cannot start a script that is already running.")
        }

        internalIsRunning.set(true)
        returnValue = executor.submit(scriptClosure.partially1(args).partially1(this))
    }

    override fun join(scriptTimeout: Long, threadTimeout: Long, timeUnit: TimeUnit): Either<Throwable, Any?> {
        // Get the result. `get` will throw an ExecutionException if the script threw an exception.
        // This suppression is fine because we treat the exception carefully and propagate it to the user.
        @SuppressWarnings("TooGenericExceptionCaught")
        val result = try {
            // A timeout of 0 means to wait forever. `get` doesn't handle this, so we have to.
            if (scriptTimeout == 0L) {
                returnValue.get().right()
            } else {
                returnValue.get(scriptTimeout, timeUnit).right()
            }
        } catch (ex: Throwable) {
            (ex as? ExecutionException)?.cause?.left() ?: ex.nonFatalOrThrow().left()
        }

        returnValue = FutureTask { Unit }

        // Join all the child threads this script started.
        threads.forEach {
            it.join(timeUnit.toMillis(threadTimeout))
            if (it.isAlive) {
                it.interrupt()
            }
        }

        // Wait until they all finish.
        threads.forEach {
            // TODO: Cap the wait time per thread to some sane maximum
            while (it.isAlive) {
                Thread.sleep(1)
            }
        }

        // Reset this script so it can be run again.
        threads.clear()
        internalIsRunning.set(false)
        return result
    }

    override fun addThread(thread: Thread) {
        threads.add(thread)
    }

    override fun resolveAndLoad(fileSpec: FileSpec, scriptEnvironment: Map<String, String>) =
        scriptLoader.resolveAndLoad(fileSpec, listOf(), scriptEnvironment)
}
