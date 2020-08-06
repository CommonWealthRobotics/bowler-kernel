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
package com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid

/**
 * The type of a resource.
 *
 * @param type The unique type of this resource.
 * @param sendLength The number of bytes required to send data to this resource.
 * @param receiveLength The number of bytes required to read data from this resource.
 */
data class ResourceType(
    val type: Byte,
    val sendLength: Byte,
    val receiveLength: Byte
)
