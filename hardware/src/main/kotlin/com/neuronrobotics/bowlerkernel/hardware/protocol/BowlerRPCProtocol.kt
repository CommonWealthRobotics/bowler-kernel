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
package com.commonwealthrobotics.bowlerkernel.hardware.protocol

import arrow.fx.IO
import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceType

/**
 * An RPC protocol that all Bowler devices implement. Read/write calls have a 5ms RTT guarantee with
 * a 100ms timeout guarantee. All methods in this interface may trigger communications with the
 * device, such as a packet being sent.
 *
 * // TODO: Merge read and write calls
 */
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
     * Adds a resource.
     *
     * @return An error if there is a problem.
     */
    fun add(resourceId: ResourceId): IO<Unit>

    /**
     * Adds a group of resources. All resources in the group are written to and read from at the
     * same time.
     *
     * @return An error if there is a problem.
     */
    fun addGroup(resourceIds: List<ResourceId>): IO<Unit>

    /**
     * Queries whether the [resourceType] is supported by this device. This should be called before
     * [isResourceInRange] to determine whether support for the [resourceType] has been compiled
     * into the device before checking if the resource is in range.
     *
     * @param resourceType The type of the resource.
     * @return Whether the resource type is supported by this device.
     */
    fun isResourceTypeSupported(resourceType: ResourceType): IO<Boolean>

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
     * Performs a read.
     *
     * @param resourceId The id of a resource on this device.
     * @return The entire read payload.
     */
    fun read(resourceId: ResourceId): IO<ByteArray>

    /**
     * Performs a read.
     *
     * @param resourceIds The resources.
     * @return The entire read payload.
     */
    fun read(resourceIds: List<ResourceId>): IO<List<ByteArray>>

    /**
     * Performs a write and a read in one RPC call (write first, then read).
     *
     * @param resourceId The id of a resource on this device.
     * @param payload The payload to send.
     * @return The entire read payload.
     */
    fun writeAndRead(resourceId: ResourceId, payload: ByteArray): IO<ByteArray>

    /**
     * Performs a write and a read in one RPC call (write first, then read).
     *
     * @param resourcesAndValues The resources paired with their payloads.
     * @return The entire read payload.
     */
    fun writeAndRead(resourcesAndValues: List<Pair<ResourceId, ByteArray>>): IO<List<ByteArray>>
}
