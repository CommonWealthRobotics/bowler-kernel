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
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly. Parameter 1 is whether the
     * resource id is in the valid range of resources for this device.
     */
    fun isResourceInRange(
        resourceId: ResourceId,
        timeout: () -> Unit = {},
        success: (Boolean) -> Unit
    )

    /**
     * Tells the device to attach and set up a resource.
     *
     * @param resourceId The id of the resource.
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly. Parameter 1 is whether the
     * resource was successfully provisioned.
     */
    fun provisionResource(
        resourceId: ResourceId,
        timeout: () -> Unit = {},
        success: (Boolean) -> Unit
    )

    /**
     * Queries the device's Bowler RPC protocol version.
     *
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly. Parameter 1 is the protocol
     * version string.
     */
    fun readProtocolVersion(
        timeout: () -> Unit = {},
        success: (String) -> Unit
    )

    /**
     * Performs an analog read.
     *
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly. Parameter 1 is the analog value.
     */
    fun analogRead(
        resourceId: ResourceId,
        timeout: () -> Unit = {},
        success: (Double) -> Unit
    )

    /**
     * Performs an analog write.
     *
     * @param value The analog value.
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly.
     */
    fun analogWrite(
        resourceId: ResourceId,
        value: Long,
        timeout: () -> Unit = {},
        success: () -> Unit
    )

    /**
     * Performs a debounced button read.
     *
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly. Parameter 1 is whether the button
     * is pressed.
     */
    fun buttonRead(
        resourceId: ResourceId,
        timeout: () -> Unit = {},
        success: (Boolean) -> Unit
    )

    /**
     * Performs a digital read.
     *
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly. Parameter 1 is the digital value.
     */
    fun digitalRead(
        resourceId: ResourceId,
        timeout: () -> Unit = {},
        success: (DigitalState) -> Unit
    )

    /**
     * Performs a digital write.
     *
     * @param value The digital value.
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly.
     */
    fun digitalWrite(
        resourceId: ResourceId,
        value: DigitalState,
        timeout: () -> Unit = {},
        success: () -> Unit
    )

    /**
     * Performs an encoder read.
     *
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly. Parameter 1 is the encoder value.
     */
    fun encoderRead(
        resourceId: ResourceId,
        timeout: () -> Unit = {},
        success: (Long) -> Unit
    )

    /**
     * Performs a tone write.
     *
     * @param frequency The frequency, in Hz, of the tone.
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly.
     */
    fun toneWrite(
        resourceId: ResourceId,
        frequency: Long,
        timeout: () -> Unit = {},
        success: () -> Unit
    )

    /**
     * Performs a tone write.
     *
     * @param frequency The frequency, in Hz, of the tone.
     * @param duration The duration of the tone.
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly.
     */
    fun toneWrite(
        resourceId: ResourceId,
        frequency: Long,
        duration: Long,
        timeout: () -> Unit = {},
        success: () -> Unit
    )

    /**
     * Performs a serial write.
     *
     * @param message The serial message.
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly.
     */
    fun serialWrite(
        resourceId: ResourceId,
        message: String,
        timeout: () -> Unit = {},
        success: () -> Unit
    )

    /**
     * Performs a serial read.
     *
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly. Parameter 1 is the serial message.
     */
    fun serialRead(
        resourceId: ResourceId,
        timeout: () -> Unit = {},
        success: (String) -> Unit
    )

    /**
     * Performs a servo write.
     *
     * @param angle The servo angle (or speed for a continuous rotation servo).
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly.
     */
    fun servoWrite(
        resourceId: ResourceId,
        angle: Double,
        timeout: () -> Unit = {},
        success: () -> Unit
    )

    /**
     * Performs a servo read.
     *
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly. Parameter 1 is the servo angle.
     */
    fun servoRead(
        resourceId: ResourceId,
        timeout: () -> Unit = {},
        success: (Double) -> Unit
    )

    /**
     * Performs an ultrasonic read.
     *
     * @param timeout Called if the device timed out.
     * @param success Called if the device responded correctly. Parameter 1 is the raw distance.
     */
    fun ultrasonicRead(
        resourceId: ResourceId,
        timeout: () -> Unit = {},
        success: (Long) -> Unit
    )
}
