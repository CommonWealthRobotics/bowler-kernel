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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

/**
 * A version of the Bowler RPC which is interfaced with synchronously.
 */
interface SynchronousBowlerRPCProtocol {

    /**
     * Opens a persistent connection to the device. An error is returned if the connection is
     * already open or could not be opened.
     *
     * @return An error if there is a connection problem.
     */
    fun connect(): Option<String>

    /**
     * Closes the persistent connection to the device. Does nothing if there is no connection open.
     */
    fun disconnect()

    /**
     * Queries whether the [resourceId] is in the valid range of resources for this device.
     *
     * @param resourceId The id of a resource on this device.
     * @return Whether the resource id is in the valid range of resources for this device.
     */
    fun CoroutineScope.isResourceInRange(resourceId: ResourceId): Deferred<Either<TimeoutError, Boolean>>

    /**
     * Tells the device to attach and set up a resource.
     *
     * @param resourceId The id of the resource.
     * @return Whether the resource was successfully provisioned.
     */
    fun CoroutineScope.provisionResource(resourceId: ResourceId): Deferred<Either<TimeoutError, Boolean>>

    /**
     * Queries the device's Bowler RPC protocol version.
     *
     * @return The protocol version string.
     */
    fun CoroutineScope.readProtocolVersion(): Deferred<Either<TimeoutError, String>>

    /**
     * Performs an analog read.
     *
     * @return The analog value.
     */
    fun CoroutineScope.analogRead(resourceId: ResourceId): Deferred<Either<TimeoutError, Double>>

    /**
     * Performs an analog write.
     *
     * @param value The analog value.
     */
    fun CoroutineScope.analogWrite(
        resourceId: ResourceId,
        value: Long
    ): Deferred<Option<TimeoutError>>

    /**
     * Performs a debounced button read.
     *
     * @return Whether the button is pressed.
     */
    fun CoroutineScope.buttonRead(resourceId: ResourceId): Deferred<Either<TimeoutError, Boolean>>

    /**
     * Performs a digital read.
     *
     * @return The digital value.
     */
    fun CoroutineScope.digitalRead(resourceId: ResourceId): Deferred<Either<TimeoutError, DigitalState>>

    /**
     * Performs a digital write.
     *
     * @param value The digital value.
     */
    fun CoroutineScope.digitalWrite(
        resourceId: ResourceId,
        value: DigitalState
    ): Deferred<Option<TimeoutError>>

    /**
     * Performs an encoder read.
     *
     * @return The encoder value.
     */
    fun CoroutineScope.encoderRead(resourceId: ResourceId): Deferred<Either<TimeoutError, Long>>

    /**
     * Performs a tone write.
     *
     * @param frequency The frequency, in Hz, of the tone.
     */
    fun CoroutineScope.toneWrite(
        resourceId: ResourceId,
        frequency: Long
    ): Deferred<Option<TimeoutError>>

    /**
     * Performs a tone write.
     *
     * @param frequency The frequency, in Hz, of the tone.
     * @param duration The duration of the tone.
     */
    fun CoroutineScope.toneWrite(
        resourceId: ResourceId,
        frequency: Long,
        duration: Long
    ): Deferred<Option<TimeoutError>>

    /**
     * Performs a serial write.
     *
     * @param message The serial message.
     */
    fun CoroutineScope.serialWrite(
        resourceId: ResourceId,
        message: String
    ): Deferred<Option<TimeoutError>>

    /**
     * Performs a serial read.
     *
     * @return The serial message.
     */
    fun CoroutineScope.serialRead(resourceId: ResourceId): Deferred<Either<TimeoutError, String>>

    /**
     * Performs a servo write.
     *
     * @param angle The servo angle (or speed for a continuous rotation servo).
     */
    fun CoroutineScope.servoWrite(
        resourceId: ResourceId,
        angle: Double
    ): Deferred<Option<TimeoutError>>

    /**
     * Performs a servo read.
     *
     * @return The servo angle.
     */
    fun CoroutineScope.servoRead(resourceId: ResourceId): Deferred<Either<TimeoutError, Double>>

    /**
     * Performs an ultrasonic read.
     *
     * @return The raw distance.
     */
    fun CoroutineScope.ultrasonicRead(resourceId: ResourceId): Deferred<Either<TimeoutError, Long>>
}
