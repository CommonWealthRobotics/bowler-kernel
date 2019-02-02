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

import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId

/**
 * A Bowler RPC which can be configured to repeatedly read sensor values and save them to a cache
 * for faster pc-side reading. Sensors which are polled will not return a [TimeoutError], except
 * for the first invocation which could require waiting for a packet.
 */
interface BufferedBowlerRPCProtocol : SynchronousBowlerRPCProtocol {

    /**
     * Starts polling the resource and saving it to a cache so reading from the resource does not
     * require a packet.
     *
     * @param resourceId The id of the resource to poll.
     */
    fun setupPolling(resourceId: ResourceId)
}
