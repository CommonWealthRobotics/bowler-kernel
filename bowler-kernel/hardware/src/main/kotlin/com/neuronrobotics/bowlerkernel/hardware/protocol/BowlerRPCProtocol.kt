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
    fun connect(): Either<String, Unit>

    /**
     * Closes the persistent connection to the device. Does nothing if there is no connection open.
     *
     * @return An error if there is a disconnect problem.
     */
    fun disconnect(): Either<String, Unit>

    /**
     * Adds a read resource which is constantly polled.
     *
     * @return An error if there is a problem.
     */
    fun addPollingRead(resourceId: ResourceId): Either<String, Unit>

    /**
     * Adds a group of read resources which is constantly polled. All resources in the group are
     * read from at the same time. If any resources are already polling reads, they are moved
     * into this group instead.
     *
     * @return An error if there is a problem.
     */
    fun addPollingReadGroup(resourceIds: ImmutableSet<ResourceId>): Either<String, Unit>

    /**
     * Adds a read resource.
     *
     * @return An error if there is a problem.
     */
    fun addRead(resourceId: ResourceId): Either<String, Unit>

    /**
     * Adds a group of read resources. All resources in the group are read from at the same time.
     * If any resources are already reads, they are moved into this group instead.
     *
     * @return An error if there is a problem.
     */
    fun addReadGroup(resourceIds: ImmutableSet<ResourceId>): Either<String, Unit>

    /**
     * Adds a write resource.
     *
     * @return An error if there is a problem.
     */
    fun addWrite(resourceId: ResourceId): Either<String, Unit>

    /**
     * Adds a group of write resources. All resources in the group are written to at the same
     * time. If any resources are already writes, they are moved into this group instead.
     *
     * @return An error if there is a problem.
     */
    fun addWriteGroup(resourceIds: ImmutableSet<ResourceId>): Either<String, Unit>

    /**
     * Queries whether the [resourceId] is in the valid range of resources for this device.
     *
     * @param resourceId The id of a resource on this device.
     * @return Whether the resource id is in the valid range of resources for this device.
     */
    fun isResourceInRange(resourceId: ResourceId): Boolean

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
     * Performs an analog read.
     *
     * @param resourceIds The resources in the group.
     * @return The analog values in the same order as [resourceIds].
     */
    fun analogRead(resourceIds: List<ResourceId>): List<Double>

    /**
     * Performs an analog write.
     *
     * @param resourceId The id of a resource on this device.
     * @param value The analog value.
     */
    fun analogWrite(resourceId: ResourceId, value: Short)

    /**
     * Performs an analog write.
     *
     * @param resourcesAndValues The resources paired with their values.
     */
    fun analogWrite(resourcesAndValues: List<Pair<ResourceId, Short>>)

    /**
     * Performs a debounced button read.
     *
     * @param resourceId The id of a resource on this device.
     * @return Whether the button is pressed.
     */
    fun buttonRead(resourceId: ResourceId): Boolean

    /**
     * Performs a debounced button read.
     *
     * @param resourceIds The resources in the group.
     * @return The whether the buttons are pressed, in the same order as [resourceIds].
     */
    fun buttonRead(resourceIds: List<ResourceId>): List<Boolean>

    /**
     * Performs a digital read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The digital value.
     */
    fun digitalRead(resourceId: ResourceId): DigitalState

    /**
     * Performs a digital read.
     *
     * @param resourceIds The resources in the group.
     * @return The digital values in the same order as [resourceIds].
     */
    fun digitalRead(resourceIds: List<ResourceId>): List<DigitalState>

    /**
     * Performs a digital write.
     *
     * @param resourceId The id of a resource on this device.
     * @param value The digital value.
     */
    fun digitalWrite(resourceId: ResourceId, value: DigitalState)

    /**
     * Performs a digital write for a group.
     *
     * @param resourcesAndValues The resources paired with their values.
     */
    fun digitalWrite(resourcesAndValues: List<Pair<ResourceId, DigitalState>>)

    /**
     * Performs an encoder read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The encoder value.
     */
    fun encoderRead(resourceId: ResourceId): Long

    /**
     * Performs an encoder read.
     *
     * @param resourceIds The resources in the group.
     * @return The encoder values in the same order as [resourceIds].
     */
    fun encoderRead(resourceIds: List<ResourceId>): List<Long>

    /**
     * Performs a tone write.
     *
     * @param resourceId The id of a resource on this device.
     * @param frequency The frequency, in Hz, of the tone.
     */
    fun toneWrite(resourceId: ResourceId, frequency: Int)

    /**
     * Performs a tone write.
     *
     * @param resourceId The id of a resource on this device.
     * @param frequency The frequency, in Hz, of the tone.
     * @param duration The duration of the tone.
     */
    fun toneWrite(resourceId: ResourceId, frequency: Int, duration: Long)

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

    // TODO: Add serialWriteRead

    /**
     * Performs a servo write.
     *
     * @param resourceId The id of a resource on this device.
     * @param angle The servo angle (or speed for a continuous rotation servo).
     */
    fun servoWrite(resourceId: ResourceId, angle: Double)

    /**
     * Performs a servo write.
     *
     * @param resourcesAndValues The resources paired with their values.
     */
    fun servoWrite(resourcesAndValues: List<Pair<ResourceId, Double>>)

    /**
     * Performs a stepper write.
     *
     * @param resourceId The id of a resource on this device.
     * @param steps The number of steps to travel.
     * @param speed The speed to rotate at.
     */
    fun stepperWrite(resourceId: ResourceId, steps: Int, speed: Int)

    /**
     * Performs a stepper write.
     *
     * @param resourcesAndValues The resources paired with their values.
     */
    fun stepperWrite(resourcesAndValues: List<Pair<ResourceId, Pair<Int, Int>>>)

    /**
     * Performs an ultrasonic read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The raw distance.
     */
    fun ultrasonicRead(resourceId: ResourceId): Long

    /**
     * Performs an ultrasonic read.
     *
     * @param resourceIds The resources in the group.
     * @return The raw distances in the same order as [resourceIds].
     */
    fun ultrasonicRead(resourceIds: List<ResourceId>): List<Long>

    /**
     * Performs a generic read (returns the entire payload).
     *
     * @param resourceId The id of a resource on this device.
     * @return The entire read payload.
     */
    fun genericRead(resourceId: ResourceId): ByteArray

    /**
     * Performs a generic read (returns the entire payload).
     *
     * @param resourceIds The resources in the group.
     * @return The entire read payload.
     */
    fun genericRead(resourceIds: List<ResourceId>): List<ByteArray>

    /**
     * Performs a generic write (writes the raw payload).
     *
     * @param resourceId The id of a resource on this device.
     * @param payload The payload to send.
     */
    fun genericWrite(resourceId: ResourceId, payload: ByteArray)

    /**
     * Performs a generic write (writes the raw payload).
     *
     * @param resourcesAndValues The resources paired with their payloads.
     */
    fun genericWrite(resourcesAndValues: List<Pair<ResourceId, ByteArray>>)

    /**
     * Performs a write and a read in one RPC call (write first, then read).
     *
     * @param resourceId The id of a resource on this device.
     * @param payload The payload to send.
     * @return The entire read payload.
     */
    fun genericWriteRead(resourceId: ResourceId, payload: ByteArray): ByteArray

    /**
     * Performs a write and a read in one RPC call (write first, then read).
     *
     * @param resourcesAndValues The resources paired with their payloads.
     * @return The entire read payload.
     */
    fun genericWriteRead(resourcesAndValues: List<Pair<ResourceId, ByteArray>>): List<ByteArray>
}
