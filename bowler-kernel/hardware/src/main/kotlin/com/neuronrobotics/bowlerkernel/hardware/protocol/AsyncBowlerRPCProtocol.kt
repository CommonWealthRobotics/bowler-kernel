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

import arrow.core.Option
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId

/**
 * An RPC protocol that all Bowler devices implement. All methods have a 5ms RTT guarantee with a
 * 100ms timeout guarantee. All methods in this interface are expected to trigger communications
 * with the device, such as a packet being sent.
 */
@SuppressWarnings("ComplexInterface", "TooManyFunctions")
interface AsyncBowlerRPCProtocol {

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
    fun isResourceInRange(resourceId: ResourceId): Boolean

    /**
     * Tells the device to attach and set up a resource.
     *
     * @param resourceId The id of the resource.
     * @return Whether the resource was provisioned.
     */
    fun provisionResource(resourceId: ResourceId): Boolean

    /**
     * Queries the device's Bowler RPC protocol version.
     *
     * @return The protocol version string.
     */
    fun readProtocolVersion(): String

    /**
     * Performs an analog read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The analog value.
     */
    fun analogRead(resourceId: ResourceId): Double

    /**
     * Performs an analog write.
     *
     * @param resourceId The id of a resource on this device.
     * @param value The analog value.
     */
    fun analogWrite(resourceId: ResourceId, value: Long)

    /**
     * Performs a debounced button read.
     *
     * @param resourceId The id of a resource on this device.
     * @return Whether the button is pressed.
     */
    fun buttonRead(resourceId: ResourceId): Boolean

    /**
     * Performs a digital read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The digital value.
     */
    fun digitalRead(resourceId: ResourceId): DigitalState

    /**
     * Performs a digital write.
     *
     * @param resourceId The id of a resource on this device.
     * @param value The digital value.
     */
    fun digitalWrite(resourceId: ResourceId, value: DigitalState)

    /**
     * Performs an encoder read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The encoder value.
     */
    fun encoderRead(resourceId: ResourceId): Long

    /**
     * Performs a tone write.
     *
     * @param resourceId The id of a resource on this device.
     * @param frequency The frequency, in Hz, of the tone.
     */
    fun toneWrite(resourceId: ResourceId, frequency: Long)

    /**
     * Performs a tone write.
     *
     * @param resourceId The id of a resource on this device.
     * @param frequency The frequency, in Hz, of the tone.
     * @param duration The duration of the tone.
     */
    fun toneWrite(resourceId: ResourceId, frequency: Long, duration: Long)

    /**
     * Performs a serial write.
     *
     * @param resourceId The id of a resource on this device.
     * @param message The serial message.
     */
    fun serialWrite(resourceId: ResourceId, message: String)

    /**
     * Performs a serial read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The serial data.
     */
    fun serialRead(resourceId: ResourceId): String

    /**
     * Performs a servo write.
     *
     * @param resourceId The id of a resource on this device.
     * @param angle The servo angle (or speed for a continuous rotation servo).
     */
    fun servoWrite(resourceId: ResourceId, angle: Double)

    /**
     * Performs a servo read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The servo angle.
     */
    fun servoRead(resourceId: ResourceId): Double

    /**
     * Performs an ultrasonic read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The raw distance.
     */
    fun ultrasonicRead(resourceId: ResourceId): Long
}