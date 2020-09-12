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
    private val count = AtomicInteger(count)
    private val mutex = Mutex(true)

    /**
     * Get the current count of the latch.
     * @return The current count of the latch.
     */
    fun count(): Int = count.get()

    /**
     * Decrement the current count of the latch.
     */
    fun countDown() {
        val previous = count.getAndUpdate { if (it == 0) 0 else it - 1 }
        if (previous == 0) throw IllegalStateException("KCountDownLatch is already at zero")
        else if (previous == 1) mutex.unlock()
    }

    /**
     * Wait for the latch to reach zero.
     */
    suspend fun await() = mutex.withLock { }
}