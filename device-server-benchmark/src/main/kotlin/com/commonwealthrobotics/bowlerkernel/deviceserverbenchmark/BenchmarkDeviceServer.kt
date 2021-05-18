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
package com.commonwealthrobotics.bowlerkernel.deviceserverbenchmark

import com.commonwealthrobotics.bowlerkernel.deviceserver.DeviceServer
import com.commonwealthrobotics.bowlerkernel.deviceserver.getPayload
import org.apache.commons.math3.stat.descriptive.moment.Mean
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation
import org.apache.commons.math3.stat.descriptive.rank.Median
import kotlin.system.measureNanoTime

/**
 * Benchmarks the [deviceServer] using the transport method the packet was registered with. A
 * `NoopPacket` should be registered on the device on the [packetId] and on the [deviceServer].
 *
 * @param deviceServer The [DeviceServer] to benchmark.
 * @param payloadSize The expected length of the payload.
 * @param packetId The id of the packet to communicate with.
 * @return A string describing the results of the benchmark.
 */
fun benchmarkDeviceServer(
    deviceServer: DeviceServer,
    payloadSize: Int,
    packetId: Byte
) {
    // Warm up JVM
    repeat(100) {
        measureNanoTime {
            deviceServer.write(
                packetId,
                getPayload(payloadSize, byteArrayOf(it.toByte()))
            )
        }
    }

    val roundTripTimes = (0..1000).map {
        measureNanoTime {
            deviceServer.write(packetId, getPayload(payloadSize, byteArrayOf(it.toByte())))
        }
    }

    val rttDoubles = roundTripTimes.map { it.toDouble() }.toDoubleArray()
    val meanTime = Mean().evaluate(rttDoubles)
    val medianTime = Median().evaluate(rttDoubles)
    val stdDev = StandardDeviation().evaluate(rttDoubles, meanTime)

    // 5ms RTT upper bound
    val (timesThatPassRTT, timesThatFailRTT) =
        roundTripTimes.partition { it <= 5000000 }

    val passPercentage = percentage(
        timesThatPassRTT.size.toDouble(),
        timesThatFailRTT.size.toDouble()
    )

    """
    |Percentage of RPC calls under 5ms: $passPercentage
    |Mean:    ${meanTime.nsToMs()}
    |Median:  ${medianTime.nsToMs()}
    |Std Dev: ${stdDev.nsToMs()}
    |Best:    ${roundTripTimes.minOrNull()?.nsToMs()}
    |Worst:   ${roundTripTimes.maxOrNull()?.nsToMs()}
    """.trimMargin()
}

fun Long.nsToMs() = times(0.000001)
fun Double.nsToMs() = times(0.000001)

fun percentage(pass: Number, fail: Number): Double =
    100 * pass.toDouble() / (pass.toDouble() + fail.toDouble())
