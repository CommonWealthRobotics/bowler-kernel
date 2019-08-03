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
package com.neuronrobotics.bowlerkernel.hardware

import arrow.core.Either
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.registry.BaseHardwareRegistry
import com.neuronrobotics.bowlerkernel.hardware.registry.HardwareRegistry
import com.neuronrobotics.bowlerkernel.hardware.registry.HardwareRegistryTracker
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A script with managed hardware access.
 */
abstract class Script {

    /**
     * Whether the script is currently running.
     */
    val isRunning
        get() = internalIsRunning.get()

    private var internalIsRunning = AtomicBoolean(false)

    /**
     * The [HardwareRegistry] this script should use.
     */
    val hardwareRegistry: HardwareRegistry
        get() = internalHardwareRegistry!!

    private var internalHardwareRegistry: HardwareRegistry? = null

    /**
     * The child threads the script created.
     */
    private val threads = mutableListOf<Thread>()

    /**
     * Runs the script on the current thread. This should exit within a reasonable amount of time;
     * essentially, don't put any very long-running, perhaps user-interactive, tasks in here;
     * instead, spawn another thread and track it with [addThread].
     *
     * @param args The arguments to the script.
     * @return The result of the script.
     */
    protected abstract fun runScript(args: ImmutableList<Any?>): Either<String, Any?>

    /**
     * Forces the script to stop. Do not call this directly. Call `stopAndCleanUp()`.
     */
    protected abstract fun stopScript()

    /**
     * Starts running the script on the current thread.
     *
     * @param args The arguments to the script.
     * @param parent The parent script calling this script.
     * @return The result of the script.
     */
    fun startScript(args: ImmutableList<Any?>, parent: Script? = null): Either<String, Any?> {
        require(parent != this) {
            "A script cannot be its own parent."
        }

        if (internalHardwareRegistry == null) {
            internalHardwareRegistry = if (parent == null) {
                BaseHardwareRegistry()
            } else {
                HardwareRegistryTracker(parent.internalHardwareRegistry!!)
            }
        }

        internalIsRunning.set(true)
        return runScript(args)
    }

    /**
     * Stops the script and unregisters/disconnects all hardware it registered. Always call this
     * after the script is done running.
     *
     * @param timeout The per-thread timeout before interrupting the thread.
     */
    fun stopAndCleanUp(timeout: Long = 1000) {
        stopScript()

        threads.forEach {
            it.join(timeout)
            if (it.isAlive) {
                it.interrupt()
            }
        }

        threads.clear()
        internalIsRunning.set(false)
    }

    /**
     * Adds a thread to the list of threads this script tracks.
     *
     * @param thread The thread to add.
     */
    protected fun addThread(thread: Thread) {
        threads.add(thread)
    }
}
