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
package com.neuronrobotics.bowlerkernel.hardware.protocol

import arrow.core.Either
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.util.concurrent.CountDownLatch

/**
 * A [SynchronousBowlerRPCProtocol] which synchronizes the methods of [AsyncBowlerRPCProtocol]
 * using a [CountDownLatch] and [CoroutineScope.async].
 *
 * @param bowlerRPCProtocol The actual Bowler RPC implementation.
 */
class DefaultSynchronousBowlerRPCProtocol(
    private val bowlerRPCProtocol: AsyncBowlerRPCProtocol
) : SynchronousBowlerRPCProtocol {

    override fun connect() = bowlerRPCProtocol.connect()

    override fun disconnect() = bowlerRPCProtocol.disconnect()

    private fun <T> CoroutineScope.protocolHelper(
        rpcMethod: (() -> Unit, (T) -> Unit) -> Unit
    ): Deferred<Either<TimeoutError, T>> = async {
        val latch = CountDownLatch(1)
        var out: Either<TimeoutError, T>? = null
        val startTime = System.currentTimeMillis()

        rpcMethod(
            {
                out = TimeoutError(System.currentTimeMillis() - startTime).left()
                latch.countDown()
            },
            {
                out = it.right()
                latch.countDown()
            }
        )

        latch.await()
        out ?: throw IllegalStateException("Return variable not initialized")
    }

    private fun CoroutineScope.protocolHelperOption(
        rpcMethod: (() -> Unit, () -> Unit) -> Unit
    ): Deferred<Option<TimeoutError>> = async {
        val latch = CountDownLatch(1)
        var out: Option<TimeoutError>? = null
        val startTime = System.currentTimeMillis()

        rpcMethod(
            {
                out = TimeoutError(System.currentTimeMillis() - startTime).toOption()
                latch.countDown()
            },
            {
                out = Option.empty()
                latch.countDown()
            }
        )

        latch.await()
        out ?: throw IllegalStateException("Return variable not initialized")
    }

    override fun CoroutineScope.isResourceInRange(
        resourceId: ResourceId
    ): Deferred<Either<TimeoutError, Boolean>> =
        protocolHelper { timeout, success ->
            bowlerRPCProtocol.isResourceInRange(resourceId, timeout, success)
        }

    override fun CoroutineScope.provisionResource(
        resourceId: ResourceId
    ): Deferred<Either<TimeoutError, Boolean>> =
        protocolHelper { timeout, success ->
            bowlerRPCProtocol.provisionResource(resourceId, timeout, success)
        }

    override fun CoroutineScope.readProtocolVersion(): Deferred<Either<TimeoutError, String>> =
        protocolHelper(bowlerRPCProtocol::readProtocolVersion)

    override fun CoroutineScope.analogRead(
        resourceId: ResourceId
    ): Deferred<Either<TimeoutError, Double>> =
        protocolHelper { timeout, success ->
            bowlerRPCProtocol.analogRead(resourceId, timeout, success)
        }

    override fun CoroutineScope.analogWrite(
        resourceId: ResourceId,
        value: Long
    ): Deferred<Option<TimeoutError>> =
        protocolHelperOption { timeout, success ->
            bowlerRPCProtocol.analogWrite(resourceId, value, timeout, success)
        }

    override fun CoroutineScope.buttonRead(
        resourceId: ResourceId
    ): Deferred<Either<TimeoutError, Boolean>> =
        protocolHelper { timeout, success ->
            bowlerRPCProtocol.buttonRead(resourceId, timeout, success)
        }

    override fun CoroutineScope.digitalRead(
        resourceId: ResourceId
    ): Deferred<Either<TimeoutError, DigitalState>> =
        protocolHelper { timeout, success ->
            bowlerRPCProtocol.digitalRead(resourceId, timeout, success)
        }

    override fun CoroutineScope.digitalWrite(
        resourceId: ResourceId,
        value: DigitalState
    ): Deferred<Option<TimeoutError>> =
        protocolHelperOption { timeout, success ->
            bowlerRPCProtocol.digitalWrite(resourceId, value, timeout, success)
        }

    override fun CoroutineScope.encoderRead(
        resourceId: ResourceId
    ): Deferred<Either<TimeoutError, Long>> =
        protocolHelper { timeout, success ->
            bowlerRPCProtocol.encoderRead(resourceId, timeout, success)
        }

    override fun CoroutineScope.toneWrite(
        resourceId: ResourceId,
        frequency: Long
    ): Deferred<Option<TimeoutError>> =
        protocolHelperOption { timeout, success ->
            bowlerRPCProtocol.toneWrite(resourceId, frequency, timeout, success)
        }

    override fun CoroutineScope.toneWrite(
        resourceId: ResourceId,
        frequency: Long,
        duration: Long
    ): Deferred<Option<TimeoutError>> =
        protocolHelperOption { timeout, success ->
            bowlerRPCProtocol.toneWrite(resourceId, frequency, duration, timeout, success)
        }

    override fun CoroutineScope.serialWrite(
        resourceId: ResourceId,
        message: String
    ): Deferred<Option<TimeoutError>> =
        protocolHelperOption { timeout, success ->
            bowlerRPCProtocol.serialWrite(resourceId, message, timeout, success)
        }

    override fun CoroutineScope.serialRead(
        resourceId: ResourceId
    ): Deferred<Either<TimeoutError, String>> =
        protocolHelper { timeout, success ->
            bowlerRPCProtocol.serialRead(resourceId, timeout, success)
        }

    override fun CoroutineScope.servoWrite(
        resourceId: ResourceId,
        angle: Double
    ): Deferred<Option<TimeoutError>> =
        protocolHelperOption { timeout, success ->
            bowlerRPCProtocol.servoWrite(resourceId, angle, timeout, success)
        }

    override fun CoroutineScope.servoRead(
        resourceId: ResourceId
    ): Deferred<Either<TimeoutError, Double>> =
        protocolHelper { timeout, success ->
            bowlerRPCProtocol.servoRead(resourceId, timeout, success)
        }

    override fun CoroutineScope.ultrasonicRead(
        resourceId: ResourceId
    ): Deferred<Either<TimeoutError, Long>> =
        protocolHelper { timeout, success ->
            bowlerRPCProtocol.ultrasonicRead(resourceId, timeout, success)
        }
}
