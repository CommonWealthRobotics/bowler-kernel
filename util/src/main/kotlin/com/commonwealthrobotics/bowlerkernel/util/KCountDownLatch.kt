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

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger

/**
 * Utility class which implements similar functionality to [java.util.concurrent.CountDownLatch],
 * but is coroutine-friendly.
 *
 * @param count The initial count of the latch.
 */
class KCountDownLatch(count: Int) {
    private val _count = AtomicInteger(count)
    private val mutex = Mutex(true)

    /**
     * Get the current count of the latch.
     * @return The current count of the latch.
     */
    val count: Int
        get() = _count.get()

    /**
     * Decrement the current count of the latch.
     */
    fun countDown() {
        val previous = _count.getAndUpdate { if (it == 0) 0 else it - 1 }
        if (previous == 0) throw IllegalStateException("KCountDownLatch is already at zero")
        else if (previous == 1) mutex.unlock()
    }

    /**
     * Wait for the latch to reach zero.
     */
    suspend fun await() = mutex.withLock { }
}
