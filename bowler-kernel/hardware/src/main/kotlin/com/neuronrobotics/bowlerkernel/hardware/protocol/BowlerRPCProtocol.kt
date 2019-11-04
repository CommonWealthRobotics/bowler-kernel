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

import arrow.effects.IO
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId

/**
 * An RPC protocol that all Bowler devices implement. All methods have a 5ms RTT guarantee with a
 * 100ms timeout guarantee. All methods in this interface are expected to trigger communications
 * with the device, such as a packet being sent.
 */
@SuppressWarnings("ComplexInterface", "TooManyFunctions")
interface BowlerRPCProtocol {

    /**
     * Opens a persistent connection to the device. An error is returned if the connection is
     * already open or could not be opened.
     *
     * @return An error if there is a connection problem.
     */
    fun connect(): IO<Unit>

    /**
     * Closes the persistent connection to the device. Does nothing if there is no connection open.
     *
     * @return An error if there is a disconnect problem.
     */
    fun disconnect(): IO<Unit>

    /**
     * Adds a read resource.
     *
     * @param resourceId The resource id.
     * @param isReliable Whether this resource uses reliable transport
     * @return An error if there is a problem.
     */
    fun addRead(resourceId: ResourceId, isReliable: Boolean = false): IO<Unit>

    /**
     * Adds a group of read resources. All resources in the group are read from at the same time.
     *
     * @param resourceIds The resource ids in the group.
     * @param isReliable Whether this resource uses reliable transport.
     * @return An error if there is a problem.
     */
    fun addReadGroup(resourceIds: ImmutableSet<ResourceId>, isReliable: Boolean = false): IO<Unit>

    /**
     * Adds a write resource.
     *
     * @param resourceId The resource id.
     * @param isReliable Whether this resource uses reliable transport
     * @return An error if there is a problem.
     */
    fun addWrite(resourceId: ResourceId, isReliable: Boolean = false): IO<Unit>

    /**
     * Adds a group of write resources. All resources in the group are written to at the same
     * time.
     *
     * @param resourceIds The resource ids in the group.
     * @param isReliable Whether this resource uses reliable transport
     * @return An error if there is a problem.
     */
    fun addWriteGroup(resourceIds: ImmutableSet<ResourceId>, isReliable: Boolean = false): IO<Unit>

    /**
     * Adds a write+read resource.
     *
     * @param resourceId The resource id.
     * @param isReliable Whether this resource uses reliable transport
     * @return An error if there is a problem.
     */
    fun addWriteRead(resourceId: ResourceId, isReliable: Boolean = false): IO<Unit>

    /**
     * Adds a group of write+read resources. All resources in the group are writen to and read from
     * at the same time.
     *
     * @param resourceIds The resource ids in the group.
     * @param isReliable Whether this resource uses reliable transport
     * @return An error if there is a problem.
     */
    fun addWriteReadGroup(
        resourceIds: ImmutableSet<ResourceId>,
        isReliable: Boolean = false
    ): IO<Unit>

    /**
     * Queries whether the [resourceId] is in the valid range of resources for this device.
     *
     * @param resourceId The id of a resource on this device.
     * @return Whether the resource id is in the valid range of resources for this device.
     */
    fun isResourceInRange(resourceId: ResourceId): IO<Boolean>

    /**
     * Queries the device's Bowler RPC protocol version.
     *
     * @return The protocol version string.
     */
    fun readProtocolVersion(): IO<String>

    /**
     * Performs an analog read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The analog value.
     */
    fun analogRead(resourceId: ResourceId): IO<Double>

    /**
     * Performs an analog read.
     *
     * @param resourceIds The resources in the group.
     * @return The analog values in the same order as [resourceIds].
     */
    fun analogRead(resourceIds: List<ResourceId>): IO<List<Double>>

    /**
     * Performs an analog write.
     *
     * @param resourceId The id of a resource on this device.
     * @param value The analog value.
     */
    fun analogWrite(resourceId: ResourceId, value: Short): IO<Unit>

    /**
     * Performs an analog write.
     *
     * @param resourcesAndValues The resources paired with their values.
     */
    fun analogWrite(resourcesAndValues: List<Pair<ResourceId, Short>>): IO<Unit>

    /**
     * Performs a debounced button read.
     *
     * @param resourceId The id of a resource on this device.
     * @return Whether the button is pressed.
     */
    fun buttonRead(resourceId: ResourceId): IO<Boolean>

    /**
     * Performs a debounced button read.
     *
     * @param resourceIds The resources in the group.
     * @return The whether the buttons are pressed, in the same order as [resourceIds].
     */
    fun buttonRead(resourceIds: List<ResourceId>): IO<List<Boolean>>

    /**
     * Performs a digital read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The digital value.
     */
    fun digitalRead(resourceId: ResourceId): IO<DigitalState>

    /**
     * Performs a digital read.
     *
     * @param resourceIds The resources in the group.
     * @return The digital values in the same order as [resourceIds].
     */
    fun digitalRead(resourceIds: List<ResourceId>): IO<List<DigitalState>>

    /**
     * Performs a digital write.
     *
     * @param resourceId The id of a resource on this device.
     * @param value The digital value.
     */
    fun digitalWrite(resourceId: ResourceId, value: DigitalState): IO<Unit>

    /**
     * Performs a digital write for a group.
     *
     * @param resourcesAndValues The resources paired with their values.
     */
    fun digitalWrite(resourcesAndValues: List<Pair<ResourceId, DigitalState>>): IO<Unit>

    /**
     * Performs an encoder read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The encoder value.
     */
    fun encoderRead(resourceId: ResourceId): IO<Long>

    /**
     * Performs an encoder read.
     *
     * @param resourceIds The resources in the group.
     * @return The encoder values in the same order as [resourceIds].
     */
    fun encoderRead(resourceIds: List<ResourceId>): IO<List<Long>>

    /**
     * Performs a tone write.
     *
     * @param resourceId The id of a resource on this device.
     * @param frequency The frequency, in Hz, of the tone.
     */
    fun toneWrite(resourceId: ResourceId, frequency: Int): IO<Unit>

    /**
     * Performs a tone write.
     *
     * @param resourceId The id of a resource on this device.
     * @param frequency The frequency, in Hz, of the tone.
     * @param duration The duration of the tone.
     */
    fun toneWrite(resourceId: ResourceId, frequency: Int, duration: Long): IO<Unit>

    /**
     * Performs a serial write.
     *
     * @param resourceId The id of a resource on this device.
     * @param message The serial message.
     */
    fun serialWrite(resourceId: ResourceId, message: String): IO<Unit>

    /**
     * Performs a serial read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The serial data.
     */
    fun serialRead(resourceId: ResourceId): IO<String>

    // TODO: Add serialWriteRead

    /**
     * Performs a servo write.
     *
     * @param resourceId The id of a resource on this device.
     * @param angle The servo angle (or speed for a continuous rotation servo).
     */
    fun servoWrite(resourceId: ResourceId, angle: Double): IO<Unit>

    /**
     * Performs a servo write.
     *
     * @param resourcesAndValues The resources paired with their values.
     */
    fun servoWrite(resourcesAndValues: List<Pair<ResourceId, Double>>): IO<Unit>

    /**
     * Performs a stepper write.
     *
     * @param resourceId The id of a resource on this device.
     * @param steps The number of steps to travel.
     * @param speed The speed to rotate at.
     */
    fun stepperWrite(resourceId: ResourceId, steps: Int, speed: Int): IO<Unit>

    /**
     * Performs a stepper write.
     *
     * @param resourcesAndValues The resources paired with their values.
     */
    fun stepperWrite(resourcesAndValues: List<Pair<ResourceId, Pair<Int, Int>>>): IO<Unit>

    /**
     * Performs an ultrasonic read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The raw distance.
     */
    fun ultrasonicRead(resourceId: ResourceId): IO<Long>

    /**
     * Performs an ultrasonic read.
     *
     * @param resourceIds The resources in the group.
     * @return The raw distances in the same order as [resourceIds].
     */
    fun ultrasonicRead(resourceIds: List<ResourceId>): IO<List<Long>>

    /**
     * Performs a generic read (returns the entire payload).
     *
     * @param resourceId The id of a resource on this device.
     * @return The entire read payload.
     */
    fun genericRead(resourceId: ResourceId): IO<ByteArray>

    /**
     * Performs a generic read (returns the entire payload).
     *
     * @param resourceIds The resources in the group.
     * @return The entire read payload.
     */
    fun genericRead(resourceIds: List<ResourceId>): IO<List<ByteArray>>

    /**
     * Performs a generic write (writes the raw payload).
     *
     * @param resourceId The id of a resource on this device.
     * @param payload The payload to send.
     */
    fun genericWrite(resourceId: ResourceId, payload: ByteArray): IO<Unit>

    /**
     * Performs a generic write (writes the raw payload).
     *
     * @param resourcesAndValues The resources paired with their payloads.
     */
    fun genericWrite(resourcesAndValues: List<Pair<ResourceId, ByteArray>>): IO<Unit>

    /**
     * Performs a write and a read in one RPC call (write first, then read).
     *
     * @param resourceId The id of a resource on this device.
     * @param payload The payload to send.
     * @return The entire read payload.
     */
    fun genericWriteRead(resourceId: ResourceId, payload: ByteArray): IO<ByteArray>

    /**
     * Performs a write and a read in one RPC call (write first, then read).
     *
     * @param resourcesAndValues The resources paired with their payloads.
     * @return The entire read payload.
     */
    fun genericWriteRead(resourcesAndValues: List<Pair<ResourceId, ByteArray>>): IO<List<ByteArray>>

    /**
     * Looks up the string representation of the [error] code.
     *
     * @param error The error code.
     * @return The string equivalent.
     */
    fun strerror(error: Byte): String
}
