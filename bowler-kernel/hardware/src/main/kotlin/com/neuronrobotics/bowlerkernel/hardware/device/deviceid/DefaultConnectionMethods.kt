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
package com.neuronrobotics.bowlerkernel.hardware.device.deviceid

import java.net.InetAddress

/**
 * The connection methods Bowler supports out-of-the-box.
 */
sealed class DefaultConnectionMethods : ConnectionMethod {

    /**
     * An internet address, typically used with UDP.
     *
     * @param inetAddress The IP address.
     */
    data class InternetAddress(val inetAddress: InetAddress) : DefaultConnectionMethods()

    /**
     * Raw HID.
     *
     * @param vid The vendor id.
     * @param pid The product id.
     */
    data class RawHID(val vid: Int, val pid: Int) : DefaultConnectionMethods()
}
