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
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.DigitalState
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
    fun connect(): Option<String>

    /**
     * Closes the persistent connection to the device. Does nothing if there is no connection open.
     */
    fun disconnect()

    /**
     * Adds a read resource which is constantly polled.
     *
     * @return An error if there is a problem.
     */
    fun addPollingRead(resourceId: ResourceId): Option<String>

    /**
     * Adds a group of read resources which is constantly polled. All resources in the group are
     * read from at the same time. If any resources are already polling reads, they are moved
     * into this group instead.
     *
     * @return An error if there is a problem.
     */
    fun addPollingReadGroup(resourceIds: ImmutableSet<ResourceId>): Option<String>

    /**
     * Adds a read resource.
     *
     * @return An error if there is a problem.
     */
    fun addRead(resourceId: ResourceId): Option<String>

    /**
     * Adds a group of read resources. All resources in the group are read from at the same time.
     * If any resources are already reads, they are moved into this group instead.
     *
     * @return An error if there is a problem.
     */
    fun addReadGroup(resourceIds: ImmutableSet<ResourceId>): Option<String>

    /**
     * Adds a write resource.
     *
     * @return An error if there is a problem.
     */
    fun addWrite(resourceId: ResourceId): Option<String>

    /**
     * Adds a group of write resources. All resources in the group are written to at the same
     * time. If any resources are already writes, they are moved into this group instead.
     *
     * @return An error if there is a problem.
     */
    fun addWriteGroup(resourceIds: ImmutableSet<ResourceId>): Option<String>

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
    fun analogRead(resourceIds: ImmutableList<ResourceId>): ImmutableList<Double>

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
    fun analogWrite(resourcesAndValues: ImmutableList<Pair<ResourceId, Short>>)

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
    fun buttonRead(resourceIds: ImmutableList<ResourceId>): ImmutableList<Boolean>

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
    fun digitalRead(resourceIds: ImmutableList<ResourceId>): ImmutableList<DigitalState>

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
    fun digitalWrite(resourcesAndValues: ImmutableList<Pair<ResourceId, DigitalState>>)

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
    fun encoderRead(resourceIds: ImmutableList<ResourceId>): ImmutableList<Long>

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
    fun servoWrite(resourcesAndValues: ImmutableList<Pair<ResourceId, Double>>)

    /**
     * Performs a servo read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The servo angle.
     */
    fun servoRead(resourceId: ResourceId): Double

    /**
     * Performs a servo read.
     *
     * @param resourceIds The resources in the group.
     * @return The servo angles in the same order as [resourceIds].
     */
    fun servoRead(resourceIds: ImmutableList<ResourceId>): ImmutableList<Double>

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
    fun ultrasonicRead(resourceIds: ImmutableList<ResourceId>): ImmutableList<Long>
}
