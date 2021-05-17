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
package com.commonwealthrobotics.bowlerkernel.lifecycle

import com.commonwealthrobotics.proto.lifecycle.KeepaliveRequest
import com.commonwealthrobotics.proto.lifecycle.KeepaliveResponse
import com.commonwealthrobotics.proto.lifecycle.LifecycleGrpcKt
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

class Lifecycle(
    keepaliveEnabled: Boolean = false,
    private val timeoutMs: Long = 5e3.toLong(),
    private val shutdownCallback: () -> Unit
) : LifecycleGrpcKt.LifecycleCoroutineImplBase() {

    private val keepaliveTime = AtomicLong(-1)

    init {
        thread(start = keepaliveEnabled, isDaemon = true, name = "lifecycle-keepalive") {
            keepaliveTime.set(System.nanoTime())
            var lastTime: Long
            do {
                lastTime = keepaliveTime.get()
                Thread.sleep(timeoutMs)
            } while (keepaliveTime.get() - lastTime > 0)

            logger.warn { "Keepalive timeout ($timeoutMs ms) exceeded. Running shutdown callback." }
            shutdownCallback()
        }
    }

    override suspend fun keepalive(request: KeepaliveRequest): KeepaliveResponse {
        keepaliveTime.set(System.nanoTime())
        return KeepaliveResponse.getDefaultInstance()
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
