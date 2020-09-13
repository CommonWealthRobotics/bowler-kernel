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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.selects.select
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Models "synchronous" method calls using coroutines. Meant to be used in combination with [select] to implement
 * "synchronous" method calls over a send channel and a receive channel.
 */
class CallbackLatch<T, R> {

    /**
     * A single request-response transaction.
     *
     * @param input The data of the request.
     * @param callback Called with the data of the response.
     */
    data class Transaction<T, R>(val input: T, val callback: (R) -> Unit)

    private val transactions = Channel<Transaction<T, R>>()

    /**
     * Meant to be used in a [select] expression.
     */
    val onReceive = transactions.onReceive

    /**
     * Perform a "synchronous" request-response transaction. Suspends until a response is received.
     *
     * @param input The data of the request.
     * @return The data of the response.
     */
    suspend fun call(input: T): R = suspendCoroutine { cont ->
        transactions.sendBlocking(Transaction(input) { cont.resume(it) })
    }
}
