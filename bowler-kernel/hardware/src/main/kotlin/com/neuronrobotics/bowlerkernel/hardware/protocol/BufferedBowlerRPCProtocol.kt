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
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId

/**
 * A very similar interface to [BowlerRPCProtocol] which can be configured to repeatedly read
 * sensor values and save them to a cache for faster pc-side reading. Sensors which are polled
 * will not return a [TimeoutError], except for the first invocation which could require waiting
 * for a packet.
 */
interface BufferedBowlerRPCProtocol {

    /**
     * Starts polling the resource and saving it to a cache so reading from the resource does not
     * require a packet.
     *
     * @param resourceId The id of the resource to poll.
     */
    fun setupPolling(resourceId: ResourceId)

    /**
     * Queries whether the [resourceId] is in the valid range of resources for this device.
     *
     * @param resourceId The id of a resource on this device.
     * @return Whether the resource id is in the valid range of resources for this device.
     */
    suspend fun isResourceInRange(resourceId: ResourceId): Either<TimeoutError, Boolean>

    /**
     * Tells the device to attach and set up a resource.
     *
     * @param resourceId The id of the resource.
     * @return Whether the resource was successfully provisioned.
     */
    suspend fun provisionResource(resourceId: ResourceId): Either<TimeoutError, Boolean>

    /**
     * Queries the device's Bowler RPC protocol version.
     *
     * @return The protocol version string.
     */
    suspend fun readProtocolVersion(): Either<TimeoutError, String>

    /**
     * Performs an analog read.
     *
     * @return The analog value.
     */
    suspend fun analogRead(resourceId: ResourceId): Either<TimeoutError, Double>

    /**
     * Performs an analog write.
     *
     * @param value The analog value.
     */
    suspend fun analogWrite(resourceId: ResourceId, value: Long): Option<TimeoutError>

    /**
     * Performs a debounced button read.
     *
     * @return Whether the button is pressed.
     */
    suspend fun buttonRead(resourceId: ResourceId): Either<TimeoutError, Boolean>

    /**
     * Performs a digital read.
     *
     * @return The digital value.
     */
    suspend fun digitalRead(resourceId: ResourceId): Either<TimeoutError, DigitalState>

    /**
     * Performs a digital write.
     *
     * @param value The digital value.
     */
    suspend fun digitalWrite(resourceId: ResourceId, value: DigitalState): Option<TimeoutError>

    /**
     * Performs an encoder read.
     *
     * @return The encoder value.
     */
    suspend fun encoderRead(resourceId: ResourceId): Either<TimeoutError, Long>

    /**
     * Performs a tone write.
     *
     * @param frequency The frequency, in Hz, of the tone.
     */
    suspend fun toneWrite(resourceId: ResourceId, frequency: Long): Option<TimeoutError>

    /**
     * Performs a tone write.
     *
     * @param frequency The frequency, in Hz, of the tone.
     * @param duration The duration of the tone.
     */
    suspend fun toneWrite(
        resourceId: ResourceId,
        frequency: Long,
        duration: Long
    ): Option<TimeoutError>

    /**
     * Performs a serial write.
     *
     * @param message The serial message.
     */
    suspend fun serialWrite(resourceId: ResourceId, message: String): Option<TimeoutError>

    /**
     * Performs a serial read.
     *
     * @return The serial message.
     */
    suspend fun serialRead(resourceId: ResourceId): Either<TimeoutError, String>

    /**
     * Performs a servo write.
     *
     * @param angle The servo angle (or speed for a continuous rotation servo).
     */
    suspend fun servoWrite(resourceId: ResourceId, angle: Double): Option<TimeoutError>

    /**
     * Performs a servo read.
     *
     * @return The servo angle.
     */
    suspend fun servoRead(resourceId: ResourceId): Either<TimeoutError, Double>

    /**
     * Performs an ultrasonic read.
     *
     * @return The raw distance.
     */
    suspend fun ultrasonicRead(resourceId: ResourceId): Either<TimeoutError, Long>
}
